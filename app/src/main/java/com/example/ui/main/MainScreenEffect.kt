package com.example.ui.main

import java.util.UUID

sealed class MainScreenEffect {
    data class ShowSnackbar(val message: String, val actionLabel: String? = null, val action: (() -> Unit)? = null) : MainScreenEffect()
    data class NavigateToEditor(val noteId: UUID?) : MainScreenEffect()
    object RequestBiometric : MainScreenEffect()
    data class ShareNote(val content: String) : MainScreenEffect()
}
