package com.example.domain.repository

import com.example.domain.model.Folder
import com.example.domain.model.Note
import com.example.domain.model.Result
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface NoteRepository {
    fun getAllRootFolders(): Flow<List<Folder>>
    fun getChildFolders(parentId: UUID): Flow<List<Folder>>
    fun getFolderById(id: UUID): Flow<Folder?>
    suspend fun insertFolder(folder: Folder): Result<Unit>
    suspend fun updateFolder(folder: Folder): Result<Unit>
    suspend fun deleteFolder(folder: Folder): Result<Unit>
    fun searchFoldersByName(query: String): Flow<List<Folder>>
    suspend fun updateOrderIndexes(folders: List<Folder>): Result<Unit>
    fun getAllFoldersFlat(): Flow<List<Folder>>
    suspend fun deleteFolderAndContents(folder: Folder): Result<Unit>

    fun getNotesInFolder(folderId: UUID?): Flow<List<Note>>
    fun getNoteById(id: UUID): Flow<Note?>
    suspend fun getNoteByIdOnce(id: UUID): Note?
    fun searchNotes(query: String): Flow<List<Note>>
    suspend fun insertNote(note: Note): Result<Unit>
    suspend fun updateNote(note: Note): Result<Unit>
    suspend fun deleteNote(note: Note): Result<Unit>
    fun searchNotesByTags(tag: String): Flow<List<Note>>
    fun getAllNoteIdsForWikiCheck(): Flow<List<UUID>>
}
