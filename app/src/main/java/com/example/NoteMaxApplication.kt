package com.example

import android.app.Application

class NoteMaxApplication : Application() {
    companion object {
        lateinit var instance: NoteMaxApplication
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
