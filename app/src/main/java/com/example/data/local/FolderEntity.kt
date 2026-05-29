package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "folders",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentFolderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["parentFolderId"])]
)
data class FolderEntity(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val parentFolderId: UUID? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val isLocked: Boolean = false,
    val orderIndex: Int = 0
)
