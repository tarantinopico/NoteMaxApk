package com.example.data.repository

import com.example.data.local.NoteEntity
import com.example.domain.model.Note

fun NoteEntity.toDomainModel(): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        folderId = folderId,
        tags = tags,
        isLocked = isLocked,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        pinned = pinned,
        orderIndex = orderIndex,
        wikiLinks = wikiLinks
    )
}

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,
        folderId = folderId,
        tags = tags,
        isLocked = isLocked,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        pinned = pinned,
        orderIndex = orderIndex,
        wikiLinks = wikiLinks
    )
}
