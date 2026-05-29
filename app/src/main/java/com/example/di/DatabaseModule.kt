package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.FolderDao
import com.example.data.local.NoteDao
import com.example.data.local.NoteMaxDatabase
import com.example.util.Constants

object DatabaseModule {

    fun provideNoteMaxDatabase(context: Context): NoteMaxDatabase {
        return Room.databaseBuilder(
            context,
            NoteMaxDatabase::class.java,
            Constants.DATABASE_NAME
        )
        // .addMigrations(NoteMaxDatabase.MIGRATION_1_2)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
    }

    fun provideFolderDao(database: NoteMaxDatabase): FolderDao {
        return database.folderDao()
    }

    fun provideNoteDao(database: NoteMaxDatabase): NoteDao {
        return database.noteDao()
    }
}
