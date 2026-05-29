package com.example.di

import com.example.data.local.FolderDao
import com.example.data.local.NoteDao
import com.example.data.repository.NoteRepositoryImpl
import com.example.domain.repository.NoteRepository

object RepositoryModule {
    
    fun provideNoteRepository(
        folderDao: FolderDao,
        noteDao: NoteDao
    ): NoteRepository {
        return NoteRepositoryImpl(
            folderDao = folderDao,
            noteDao = noteDao
        )
    }
}
