package com.example.di

import com.example.data.repository.NoteRepositoryImpl
import com.example.domain.repository.NoteRepository
import com.example.NoteMaxApplication

object RepositoryModule {
    
    val database by lazy {
        DatabaseModule.provideNoteMaxDatabase(NoteMaxApplication.instance)
    }

    val noteRepository: NoteRepository by lazy {
        NoteRepositoryImpl(
            folderDao = database.folderDao(),
            noteDao = database.noteDao()
        )
    }
}
