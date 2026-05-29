package com.example.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Note
import com.example.domain.repository.NoteRepository
import com.example.security.CryptoManager
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class EditorState(
    val id: UUID = UUID.randomUUID(),
    val title: String = "",
    val content: String = "",
    val tags: List<String> = emptyList(),
    val isLocked: Boolean = false,
    val isPreviewMode: Boolean = false,
    val folderId: UUID? = null,
    val isLoading: Boolean = false
)

@OptIn(FlowPreview::class)
class EditorViewModel(
    private val repository: NoteRepository
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return EditorViewModel(com.example.NoteMaxApplication.instance.repository) as T
            }
        }
    }

    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    private var initialNoteLoaded = false

    init {
        viewModelScope.launch {
            _state.drop(1).debounce(1500).collect { currentState ->
                if (initialNoteLoaded) saveNote(currentState)
            }
        }
    }

    fun loadNote(id: UUID?) {
        viewModelScope.launch {
            if (id == null) {
                initialNoteLoaded = true
                return@launch
            }
            
            val note = repository.getNoteByIdOnce(id)
            if (note != null) {
                val content = if (note.isLocked) {
                    CryptoManager.decrypt(note.content)
                } else note.content
                
                _state.value = EditorState(
                    id = note.id,
                    title = note.title,
                    content = content,
                    tags = note.tags,
                    isLocked = note.isLocked,
                    folderId = note.folderId,
                    isPreviewMode = false
                )
                initialNoteLoaded = true
            }
        }
    }

    fun updateTitle(newTitle: String) {
        _state.update { it.copy(title = newTitle) }
    }

    fun updateContent(newContent: String) {
        _state.update { it.copy(content = newContent) }
    }

    fun togglePreview() {
        _state.update { it.copy(isPreviewMode = !it.isPreviewMode) }
    }

    fun toggleLock() {
        _state.update { it.copy(isLocked = !it.isLocked) }
    }

    fun insertImageIntoContent(markdownImage: String) {
        val currentContent = _state.value.content
        _state.update { it.copy(content = currentContent + "\n" + markdownImage + "\n") }
    }

    fun addTag(tag: String) {
        if (!_state.value.tags.contains(tag)) {
            _state.update { it.copy(tags = it.tags + tag) }
        }
    }

    private suspend fun saveNote(state: EditorState) {
        val finalContent = if (state.isLocked) {
            CryptoManager.encrypt(state.content)
        } else {
            state.content
        }
        
        val note = Note(
            id = state.id,
            title = state.title,
            content = finalContent,
            folderId = state.folderId,
            tags = state.tags,
            isLocked = state.isLocked,
            modifiedAt = System.currentTimeMillis()
        )
        repository.insertNote(note)
    }
}
