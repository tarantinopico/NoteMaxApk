package com.example.ui.navigation

import java.util.UUID

sealed class Screen(val route: String) {
    object Folders : Screen("folders")
    data class Folder(val id: UUID) : Screen("folder/{folderId}") {
        companion object {
            const val ROUTE = "folder/{folderId}"
            fun createRoute(folderId: UUID) = "folder/${folderId}"
        }
    }
    data class Editor(val noteId: UUID?) : Screen("editor?noteId={noteId}") {
        companion object {
            const val ROUTE = "editor?noteId={noteId}"
            fun createRoute(noteId: UUID?) = if (noteId != null) "editor?noteId=${noteId}" else "editor"
        }
    }
    object Settings : Screen("settings")
    object Search : Screen("search")
}
