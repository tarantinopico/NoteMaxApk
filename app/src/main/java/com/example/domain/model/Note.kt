package com.example.domain.model

import java.util.UUID

data class Note(
    val id: UUID = UUID.randomUUID(),
    val title: String = "",
    val content: String = "",
    val folderId: UUID? = null,
    val tags: List<String> = emptyList(),
    val isLocked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val pinned: Boolean = false,
    val orderIndex: Int = 0,
    val wikiLinks: List<String>? = emptyList()
)
