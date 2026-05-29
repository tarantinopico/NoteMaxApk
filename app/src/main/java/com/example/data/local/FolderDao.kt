package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders WHERE parentFolderId IS NULL ORDER BY orderIndex ASC")
    fun getAllRootFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE parentFolderId = :parentId ORDER BY orderIndex ASC")
    fun getChildFolders(parentId: UUID): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE id = :id")
    fun getFolderById(id: UUID): Flow<FolderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity)

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Delete
    suspend fun deleteFolder(folder: FolderEntity)

    @Query("SELECT * FROM folders WHERE name LIKE '%' || :query || '%' ORDER BY orderIndex ASC")
    fun searchFoldersByName(query: String): Flow<List<FolderEntity>>

    @Transaction
    @Update
    suspend fun updateOrderIndexes(folders: List<FolderEntity>)

    @Query("SELECT * FROM folders")
    fun getAllFoldersFlat(): Flow<List<FolderEntity>>
    
    @Query("SELECT * FROM folders WHERE parentFolderId = :parentId")
    suspend fun getChildFoldersOnce(parentId: UUID): List<FolderEntity>
}
