package com.example.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Folder
import com.example.domain.model.Note
import com.example.domain.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest

data class MainState(
    val currentFolderId: UUID? = null,
    val folders: List<Folder> = emptyList(),
    val notes: List<Note> = emptyList(),
    val breadcrumbs: List<Folder> = emptyList()
)

class MainViewModel(
    private val repository: NoteRepository
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(com.example.NoteMaxApplication.instance.repository) as T
            }
        }
    }

    private val currentFolderId = MutableStateFlow<UUID?>(null)
    private val breadcrumbs = MutableStateFlow<List<Folder>>(emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<MainState> = currentFolderId.flatMapLatest { folderId ->
        combine(
            if (folderId == null) repository.getAllRootFolders() else repository.getChildFolders(folderId),
            repository.getNotesInFolder(folderId)
        ) { folders, notes ->
            MainState(
                currentFolderId = folderId,
                folders = folders,
                notes = notes,
                breadcrumbs = breadcrumbs.value
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainState()
    )

    fun navigateToFolder(folderId: UUID?) {
        viewModelScope.launch {
            if (folderId == null) {
                breadcrumbs.value = emptyList()
            } else {
                val current = breadcrumbs.value
                val index = current.indexOfFirst { it.id == folderId }
                if (index >= 0) {
                    breadcrumbs.value = current.take(index + 1)
                } else {
                    repository.getFolderById(folderId).collect { f ->
                        if (f != null && current.none { it.id == f.id }) {
                            breadcrumbs.value = current + f
                        }
                    }
                }
            }
            currentFolderId.value = folderId
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            repository.insertFolder(Folder(name = name, parentFolderId = currentFolderId.value))
        }
    }
}
