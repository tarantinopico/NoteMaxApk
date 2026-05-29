package com.example

import android.app.Application
import com.example.data.local.NoteMaxDatabase
import com.example.data.repository.NoteRepositoryImpl
import com.example.domain.repository.NoteRepository
import com.example.util.Constants
import androidx.room.Room

class NoteMaxApplication : Application() {
    companion object {
        lateinit var instance: NoteMaxApplication
            private set
    }
    
    val database by lazy {
        Room.databaseBuilder(
            this,
            NoteMaxDatabase::class.java,
            Constants.DATABASE_NAME
        )
        .addMigrations(NoteMaxDatabase.MIGRATION_1_2)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
    }
    
    val repository: NoteRepository by lazy {
        NoteRepositoryImpl(database.folderDao(), database.noteDao())
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}

