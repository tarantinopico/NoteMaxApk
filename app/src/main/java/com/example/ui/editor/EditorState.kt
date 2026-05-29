package com.example.ui.editor

import com.example.domain.model.Note
import java.util.UUID

sealed class EditorEvent {
    data class TitleChanged(val title: String) : EditorEvent()
    data class ContentChanged(val content: String) : EditorEvent()
    data class TagsChanged(val tags: List<String>) : EditorEvent()
    object ToggleLock : EditorEvent()
    object RequestSave : EditorEvent()
    data class SuggestWikiLinks(val prefix: String) : EditorEvent()
    data class ResolveWikiLink(val title: String) : EditorEvent()
    data class BiometricAuthenticated(val success: Boolean) : EditorEvent()
    data class ImageSelected(val uriString: String?) : EditorEvent()
}

data class EditorState(
    val note: Note? = null,
    val title: String = "",
    val content: String = "",
    val tags: List<String> = emptyList(),
    val isSaving: Boolean = false,
    val isLocked: Boolean = false,
    val showBiometricPrompt: Boolean = false,
    val isUnlockedForSession: Boolean = false,
    val wikiSuggestions: List<Note> = emptyList()
)

sealed class EditorEffect {
    object RequestBiometric : EditorEffect()
    data class ShowSnackbar(val message: String) : EditorEffect()
    data class NavigateToNote(val noteId: UUID) : EditorEffect()
    object NavigateBack : EditorEffect()
}
