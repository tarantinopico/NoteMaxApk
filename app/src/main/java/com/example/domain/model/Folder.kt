package com.example.domain.model

import java.util.UUID

data class Folder(
    val id: UUID,
    val name: String,
    val parentFolderId: UUID?,
    val createdAt: Long,
    val modifiedAt: Long,
    val isLocked: Boolean,
    val orderIndex: Int
)
