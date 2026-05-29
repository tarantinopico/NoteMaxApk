package com.example

import android.app.Application
import com.example.di.DatabaseModule
import com.example.di.RepositoryModule

class NoteMaxApplication : Application() {
    companion object {
        lateinit var instance: NoteMaxApplication
            private set
    }
    
    val database by lazy {
        DatabaseModule.provideNoteMaxDatabase(this)
    }

    val repository by lazy {
        RepositoryModule.provideNoteRepository(
            folderDao = database.folderDao(),
            noteDao = database.noteDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
