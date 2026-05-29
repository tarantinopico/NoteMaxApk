package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["folderId"])
    ]
)
data class NoteEntity(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val content: String = "",
    val folderId: UUID? = null,
    val tags: List<String> = emptyList(),
    val isLocked: Boolean = false,
    val drawingData: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val pinned: Boolean = false,
    val orderIndex: Int = 0,
    val wikiLinks: List<String>? = emptyList()
)
