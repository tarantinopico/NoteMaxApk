package com.example.ui.main

import com.example.domain.model.Folder
import com.example.domain.model.Note
import java.util.UUID

data class MainScreenState(
    val folders: List<Folder> = emptyList(),
    val notes: List<Note> = emptyList(),
    val currentPath: List<Folder> = emptyList(),
    val searchResultsNotes: List<Note> = emptyList(),
    val searchResultsFolders: List<Folder> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isSelectionMode: Boolean = false,
    val selectedIds: Set<UUID> = emptySet(),
    val error: String? = null
)
