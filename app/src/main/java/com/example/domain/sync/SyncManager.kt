package com.example.domain.sync

import android.content.Context
import android.util.Log
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class SyncManager(private val context: Context) {
    fun sync() {
        Log.d("SyncManager", "Sync not configured")
    }

    fun scheduleSync() {
        /*
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueue(syncRequest)
        */
    }
}
