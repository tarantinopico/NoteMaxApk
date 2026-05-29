package com.example.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.di.DispatcherProvider
import com.example.domain.model.Folder
import com.example.domain.model.Note
import com.example.domain.model.Result
import com.example.domain.repository.NoteRepository
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
import com.example.di.RepositoryModule

class MainViewModel(
    private val repository: NoteRepository = RepositoryModule.noteRepository,
    private val dispatchers: DispatcherProvider = com.example.di.DefaultDispatcherProvider()
) : ViewModel() {

    private val _state = MutableStateFlow(MainScreenState(isLoading = true))
    val state: StateFlow<MainScreenState> = _state.asStateFlow()

    private val _effects = Channel<MainScreenEffect>()
    val effects = _effects.receiveAsFlow()

    private var folderJob: Job? = null
    private var noteJob: Job? = null
    private var searchJob: Job? = null
    
    private var recentlyDeletedNote: Note? = null

    init {
        loadData(null)
    }

    fun processEvent(event: MainScreenEvent) {
        when (event) {
            is MainScreenEvent.LoadRoot -> {
                _state.update { it.copy(currentPath = emptyList()) }
                loadData(null)
            }
            is MainScreenEvent.NavigateIntoFolder -> {
                _state.update { it.copy(currentPath = it.currentPath + event.folder, isSearchActive = false, searchQuery = "") }
                loadData(event.folder.id)
            }
            is MainScreenEvent.NavigateUp -> {
                val newPath = _state.value.currentPath.dropLast(1)
                _state.update { it.copy(currentPath = newPath) }
                loadData(newPath.lastOrNull()?.id)
            }
            is MainScreenEvent.CreateFolder -> createFolder(event.name)
            is MainScreenEvent.CreateNote -> createNote(event.title, event.content)
            is MainScreenEvent.TogglePin -> togglePin(event.note)
            is MainScreenEvent.DeleteNote -> deleteNote(event.note)
            is MainScreenEvent.UndoDeleteNote -> undoDeleteNote(event.note)
            is MainScreenEvent.DeleteFolder -> deleteFolder(event.folder)
            is MainScreenEvent.MoveNote -> moveNote(event.note, event.newFolderId)
            is MainScreenEvent.RenameFolder -> renameFolder(event.folder, event.newName)
            is MainScreenEvent.SearchQueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
                performSearch(event.query)
            }
            is MainScreenEvent.ToggleSearch -> {
                val newSearchActive = !_state.value.isSearchActive
                _state.update { it.copy(isSearchActive = newSearchActive, searchQuery = "") }
                if (!newSearchActive) {
                    loadData(_state.value.currentPath.lastOrNull()?.id)
                }
            }
            is MainScreenEvent.ToggleSelectionMode -> {
                _state.update { it.copy(isSelectionMode = event.active, selectedIds = emptySet()) }
            }
            is MainScreenEvent.ToggleNoteSelection -> {
                val current = _state.value.selectedIds.toMutableSet()
                if (current.contains(event.noteId)) {
                    current.remove(event.noteId)
                } else {
                    current.add(event.noteId)
                }
                _state.update { it.copy(selectedIds = current) }
            }
            is MainScreenEvent.ClearSelection -> {
                _state.update { it.copy(isSelectionMode = false, selectedIds = emptySet()) }
            }
        }
    }

    private fun loadData(folderId: UUID?) {
        _state.update { it.copy(isLoading = true) }
        folderJob?.cancel()
        noteJob?.cancel()

        folderJob = viewModelScope.launch(dispatchers.io) {
            val flow = if (folderId == null) repository.getAllRootFolders() else repository.getChildFolders(folderId)
            flow.collectLatest { list ->
                _state.update { it.copy(folders = list, isLoading = false) }
            }
        }

        noteJob = viewModelScope.launch(dispatchers.io) {
            repository.getNotesInFolder(folderId).collectLatest { list ->
                _state.update { it.copy(notes = list, isLoading = false) }
            }
        }
    }

    private fun performSearch(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _state.update { it.copy(searchResultsNotes = emptyList(), searchResultsFolders = emptyList()) }
            return
        }
        searchJob = viewModelScope.launch(dispatchers.io) {
            launch {
                repository.searchNotes(query).collectLatest { list ->
                    _state.update { it.copy(searchResultsNotes = list) }
                }
            }
            launch {
                repository.searchFoldersByName(query).collectLatest { list ->
                    _state.update { it.copy(searchResultsFolders = list) }
                }
            }
        }
    }

    private fun createFolder(name: String) {
        viewModelScope.launch(dispatchers.io) {
            val parentId = _state.value.currentPath.lastOrNull()?.id
            val folder = Folder(
                id = UUID.randomUUID(),
                name = name,
                parentFolderId = parentId,
                createdAt = System.currentTimeMillis(),
                modifiedAt = System.currentTimeMillis(),
                isLocked = false,
                orderIndex = 0
            )
            val result = repository.insertFolder(folder)
            if (result is Result.Failure) {
                _effects.send(MainScreenEffect.ShowSnackbar("Failed to create folder"))
            }
        }
    }

    private fun createNote(title: String, content: String) {
        viewModelScope.launch(dispatchers.io) {
            val parentId = _state.value.currentPath.lastOrNull()?.id
            val note = Note(
                id = UUID.randomUUID(),
                title = title,
                content = content,
                folderId = parentId,
                tags = emptyList(),
                isLocked = false,
                createdAt = System.currentTimeMillis(),
                modifiedAt = System.currentTimeMillis(),
                pinned = false,
                orderIndex = 0,
                wikiLinks = emptyList()
            )
            repository.insertNote(note)
        }
    }

    private fun togglePin(note: Note) {
        viewModelScope.launch(dispatchers.io) {
            repository.updateNote(note.copy(pinned = !note.pinned, modifiedAt = System.currentTimeMillis()))
        }
    }

    private fun deleteNote(note: Note) {
        viewModelScope.launch(dispatchers.io) {
            // Optimistic update handled by flow returning immediately when item deleted from DB
            recentlyDeletedNote = note
            val result = repository.deleteNote(note)
            if (result is Result.Success) {
                _effects.send(MainScreenEffect.ShowSnackbar("Note deleted", "UNDO") {
                    processEvent(MainScreenEvent.UndoDeleteNote(note))
                })
            } else {
                 _effects.send(MainScreenEffect.ShowSnackbar("Failed to delete note"))
            }
        }
    }

    private fun undoDeleteNote(note: Note) {
        viewModelScope.launch(dispatchers.io) {
            repository.insertNote(note)
        }
    }

    private fun deleteFolder(folder: Folder) {
        viewModelScope.launch(dispatchers.io) {
            val result = repository.deleteFolderAndContents(folder)
            if (result is Result.Failure) {
                _effects.send(MainScreenEffect.ShowSnackbar("Failed to delete folder"))
            }
        }
    }

    private fun moveNote(note: Note, newFolderId: UUID?) {
        viewModelScope.launch(dispatchers.io) {
            repository.updateNote(note.copy(folderId = newFolderId, modifiedAt = System.currentTimeMillis()))
        }
    }

    private fun renameFolder(folder: Folder, newName: String) {
        viewModelScope.launch(dispatchers.io) {
            repository.updateFolder(folder.copy(name = newName, modifiedAt = System.currentTimeMillis()))
        }
    }
}
