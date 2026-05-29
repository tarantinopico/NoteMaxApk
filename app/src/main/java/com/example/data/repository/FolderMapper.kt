package com.example.data.repository

import com.example.data.local.FolderEntity
import com.example.domain.model.Folder

fun FolderEntity.toDomainModel(): Folder {
    return Folder(
        id = id,
        name = name,
        parentFolderId = parentFolderId,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        isLocked = isLocked,
        orderIndex = orderIndex
    )
}

fun Folder.toEntity(): FolderEntity {
    return FolderEntity(
        id = id,
        name = name,
        parentFolderId = parentFolderId,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        isLocked = isLocked,
        orderIndex = orderIndex
    )
}
