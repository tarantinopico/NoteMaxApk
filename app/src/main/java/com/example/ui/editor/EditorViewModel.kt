package com.example.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.di.DefaultDispatcherProvider
import com.example.di.DispatcherProvider
import com.example.di.RepositoryModule
import com.example.domain.model.Note
import com.example.domain.model.Result
import com.example.domain.repository.NoteRepository
import com.example.util.CryptoManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class EditorViewModel(
    private val noteId: UUID?,
    private val repository: NoteRepository = RepositoryModule.noteRepository,
    private val cryptoManager: CryptoManager = CryptoManager(),
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : ViewModel() {

    private val _state = MutableStateFlow(EditorState(isLocked = false))
    val state: StateFlow<EditorState> = _state.asStateFlow()

    private val _effects = Channel<EditorEffect>()
    val effects = _effects.receiveAsFlow()

    private var initialLoadCompleted = false
    private var saveJob: Job? = null
    private var isDirty = false

    init {
        loadNote()
    }

    private fun loadNote() {
        if (noteId == null) {
            initialLoadCompleted = true
            return
        }
        viewModelScope.launch(dispatchers.io) {
            repository.getNoteById(noteId).collect { note ->
                if (note != null && !initialLoadCompleted) {
                    initialLoadCompleted = true
                    
                    if (note.isLocked) {
                        _state.update { it.copy(
                            note = note,
                            isLocked = true,
                            showBiometricPrompt = true
                        ) }
                        _effects.send(EditorEffect.RequestBiometric)
                    } else {
                        _state.update { it.copy(
                            note = note,
                            title = note.title,
                            content = note.content,
                            tags = note.tags,
                            isLocked = false,
                            isUnlockedForSession = true
                        ) }
                    }
                }
            }
        }
    }

    fun processEvent(event: EditorEvent) {
        when (event) {
            is EditorEvent.TitleChanged -> {
                _state.update { it.copy(title = event.title) }
                isDirty = true
                autoSave()
            }
            is EditorEvent.ContentChanged -> {
                _state.update { it.copy(content = event.content) }
                isDirty = true
                autoSave()
            }
            is EditorEvent.TagsChanged -> {
                _state.update { it.copy(tags = event.tags) }
                isDirty = true
                autoSave()
            }
            is EditorEvent.ToggleLock -> {
                val newLockState = !_state.value.isLocked
                _state.update { it.copy(isLocked = newLockState) }
                isDirty = true
                autoSave()
                viewModelScope.launch {
                    _effects.send(EditorEffect.ShowSnackbar(if (newLockState) "Note locked" else "Note unlocked"))
                }
            }
            is EditorEvent.RequestSave -> saveNote()
            is EditorEvent.SuggestWikiLinks -> loadWikiSuggestions(event.prefix)
            is EditorEvent.ResolveWikiLink -> resolveWikiLink(event.title)
            is EditorEvent.BiometricAuthenticated -> {
                if (event.success) {
                    val note = _state.value.note
                    if (note != null) {
                        val decryptedContent = cryptoManager.decrypt(note.content)
                        _state.update { it.copy(
                            title = note.title,
                            content = decryptedContent,
                            tags = note.tags,
                            showBiometricPrompt = false,
                            isUnlockedForSession = true
                        ) }
                    }
                } else {
                    _state.update { it.copy(showBiometricPrompt = false) }
                    viewModelScope.launch {
                        _effects.send(EditorEffect.ShowSnackbar("Authentication failed"))
                        _effects.send(EditorEffect.NavigateBack)
                    }
                }
            }
            is EditorEvent.ImageSelected -> {
                event.uriString?.let { uri ->
                    val newContent = _state.value.content + "\n![Image]($uri)\n"
                    _state.update { it.copy(content = newContent) }
                    isDirty = true
                    autoSave()
                }
            }
        }
    }

    private fun autoSave() {
        // Debounced save could be implemented here
    }

    fun saveNote() {
        if (!isDirty && _state.value.note != null) return
        
        viewModelScope.launch(dispatchers.io) {
            _state.update { it.copy(isSaving = true) }
            val currentNote = _state.value.note
            val contentToSave = if (_state.value.isLocked) {
                cryptoManager.encrypt(_state.value.content)
            } else {
                _state.value.content
            }
            
            val noteToSave = currentNote?.copy(
                title = _state.value.title,
                content = contentToSave,
                tags = _state.value.tags,
                isLocked = _state.value.isLocked,
                modifiedAt = System.currentTimeMillis()
            ) ?: Note(
                id = noteId ?: UUID.randomUUID(),
                title = _state.value.title,
                content = contentToSave,
                folderId = null,
                tags = _state.value.tags,
                isLocked = _state.value.isLocked,
                createdAt = System.currentTimeMillis(),
                modifiedAt = System.currentTimeMillis(),
                pinned = false,
                orderIndex = 0,
                wikiLinks = extractWikiLinks(_state.value.content)
            )

            val result = if (currentNote == null) repository.insertNote(noteToSave) else repository.updateNote(noteToSave)
            
            _state.update { it.copy(note = noteToSave, isSaving = false) }
            isDirty = false
            
            if (result is Result.Failure) {
                _effects.send(EditorEffect.ShowSnackbar("Failed to save note"))
            }
        }
    }

    private fun extractWikiLinks(text: String): List<String> {
        val regex = Regex("\\[\\[(.*?)\\]\\]")
        return regex.findAll(text).map { it.groupValues[1] }.toList()
    }

    private fun loadWikiSuggestions(prefix: String) {
        if (prefix.isBlank()) {
            _state.update { it.copy(wikiSuggestions = emptyList()) }
            return
        }
        viewModelScope.launch(dispatchers.io) {
            repository.searchNotes(prefix).collectLatest { results ->
                _state.update { it.copy(wikiSuggestions = results.take(5)) }
            }
        }
    }

    private fun resolveWikiLink(title: String) {
        viewModelScope.launch(dispatchers.io) {
            repository.searchNotes(title).collect { results ->
                val exactMatch = results.firstOrNull { it.title.equals(title, ignoreCase = true) }
                if (exactMatch != null) {
                    _effects.send(EditorEffect.NavigateToNote(exactMatch.id))
                } else {
                    _effects.send(EditorEffect.ShowSnackbar("Note '$title' not found"))
                }
            }
        }
    }

    override fun onCleared() {
        saveNote()
        super.onCleared()
    }

    companion object {
        fun provideFactory(noteId: UUID?): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return EditorViewModel(noteId) as T
            }
        }
    }
}
