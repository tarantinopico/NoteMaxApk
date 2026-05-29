package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE folderId IS :folderId ORDER BY pinned DESC, orderIndex ASC, modifiedAt DESC")
    fun getNotesInFolder(folderId: UUID?): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: UUID): Flow<NoteEntity?>
    
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteByIdOnce(id: UUID): NoteEntity?

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("SELECT * FROM notes WHERE tags LIKE '%' || :tag || '%'")
    fun searchNotesByTags(tag: String): Flow<List<NoteEntity>>

    @Query("SELECT id FROM notes")
    fun getAllNoteIdsForWikiCheck(): Flow<List<UUID>>
}
