package com.example.ui.main

import com.example.domain.model.Folder
import com.example.domain.model.Note
import java.util.UUID

sealed class MainScreenEvent {
    object LoadRoot : MainScreenEvent()
    data class NavigateIntoFolder(val folder: Folder) : MainScreenEvent()
    object NavigateUp : MainScreenEvent()
    data class CreateFolder(val name: String) : MainScreenEvent()
    data class CreateNote(val title: String, val content: String) : MainScreenEvent()
    data class TogglePin(val note: Note) : MainScreenEvent()
    data class DeleteNote(val note: Note) : MainScreenEvent()
    data class DeleteFolder(val folder: Folder) : MainScreenEvent()
    data class UndoDeleteNote(val note: Note) : MainScreenEvent()
    data class MoveNote(val note: Note, val newFolderId: UUID?) : MainScreenEvent()
    data class RenameFolder(val folder: Folder, val newName: String) : MainScreenEvent()
    data class SearchQueryChanged(val query: String) : MainScreenEvent()
    object ToggleSearch : MainScreenEvent()
    data class ToggleSelectionMode(val active: Boolean) : MainScreenEvent()
    data class ToggleNoteSelection(val noteId: UUID) : MainScreenEvent()
    object ClearSelection : MainScreenEvent()
}
