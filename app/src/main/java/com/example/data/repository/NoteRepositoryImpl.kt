package com.example.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.example.data.local.FolderDao
import com.example.data.local.NoteDao
import com.example.domain.model.Folder
import com.example.domain.model.Note
import com.example.domain.model.Result
import com.example.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
class NoteRepositoryImpl(
    private val folderDao: FolderDao,
    private val noteDao: NoteDao
) : NoteRepository {
    override fun getAllRootFolders(): Flow<List<Folder>> {
        return folderDao.getAllRootFolders().map { list -> list.map { it.toDomainModel() } }
    }

    override fun getChildFolders(parentId: UUID): Flow<List<Folder>> {
        return folderDao.getChildFolders(parentId).map { list -> list.map { it.toDomainModel() } }
    }

    override fun getFolderById(id: UUID): Flow<Folder?> {
        return folderDao.getFolderById(id).map { it?.toDomainModel() }
    }

    override suspend fun insertFolder(folder: Folder): Result<Unit> {
        return try {
            folderDao.insertFolder(folder.toEntity())
            Result.Success(Unit)
        } catch (e: SQLiteConstraintException) {
            Result.Failure(e)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun updateFolder(folder: Folder): Result<Unit> {
        return try {
            folderDao.updateFolder(folder.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun deleteFolder(folder: Folder): Result<Unit> {
        return try {
            folderDao.deleteFolder(folder.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override fun searchFoldersByName(query: String): Flow<List<Folder>> {
        return folderDao.searchFoldersByName(query).map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun updateOrderIndexes(folders: List<Folder>): Result<Unit> {
        return try {
            folderDao.updateOrderIndexes(folders.map { it.toEntity() })
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override fun getAllFoldersFlat(): Flow<List<Folder>> {
        return folderDao.getAllFoldersFlat().map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun deleteFolderAndContents(folder: Folder): Result<Unit> {
        return try {
            // Room's ON DELETE CASCADE will handle subfolders and notes automatically
            folderDao.deleteFolder(folder.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override fun getNotesInFolder(folderId: UUID?): Flow<List<Note>> {
        return noteDao.getNotesInFolder(folderId).map { list -> list.map { it.toDomainModel() } }
    }

    override fun getNoteById(id: UUID): Flow<Note?> {
        return noteDao.getNoteById(id).map { it?.toDomainModel() }
    }

    override suspend fun getNoteByIdOnce(id: UUID): Note? {
        return try {
             noteDao.getNoteByIdOnce(id)?.toDomainModel()
        } catch (e: Exception) {
            null
        }
    }

    override fun searchNotes(query: String): Flow<List<Note>> {
        return noteDao.searchNotes(query).map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun insertNote(note: Note): Result<Unit> {
        return try {
            noteDao.insertNote(note.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun updateNote(note: Note): Result<Unit> {
        return try {
            noteDao.updateNote(note.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun deleteNote(note: Note): Result<Unit> {
        return try {
            noteDao.deleteNote(note.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override fun searchNotesByTags(tag: String): Flow<List<Note>> {
         return noteDao.searchNotesByTags(tag).map { list -> list.map { it.toDomainModel() } }
    }

    override fun getAllNoteIdsForWikiCheck(): Flow<List<UUID>> {
        return noteDao.getAllNoteIdsForWikiCheck()
    }
}
