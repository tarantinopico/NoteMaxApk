package com.example.domain.model

import java.util.UUID

data class Note(
    val id: UUID,
    val title: String,
    val content: String,
    val folderId: UUID?,
    val tags: List<String>,
    val isLocked: Boolean,
    val createdAt: Long,
    val modifiedAt: Long,
    val pinned: Boolean,
    val orderIndex: Int,
    val wikiLinks: List<String>?
)
