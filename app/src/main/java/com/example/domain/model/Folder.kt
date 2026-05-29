package com.example.domain.model

import java.util.UUID

data class Folder(
    val id: UUID = UUID.randomUUID(),
    val name: String = "",
    val parentFolderId: UUID? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val isLocked: Boolean = false,
    val orderIndex: Int = 0
)
