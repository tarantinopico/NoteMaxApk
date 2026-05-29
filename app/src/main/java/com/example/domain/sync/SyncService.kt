package com.example.domain.sync

import android.util.Log

interface SyncService {
    suspend fun syncNotes()
    suspend fun syncFolders()
}

class SyncManagerImpl : SyncService {
    override suspend fun syncNotes() {
        Log.d("SyncService", "Sync notes not implemented - future cloud feature")
    }

    override suspend fun syncFolders() {
        Log.d("SyncService", "Sync folders not implemented - future cloud feature")
    }
}
