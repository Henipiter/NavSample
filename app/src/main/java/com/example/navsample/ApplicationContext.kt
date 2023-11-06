package com.example.navsample

import android.app.Application
import android.content.Context

class ApplicationContext : Application() {
    override fun onCreate() {
        instance = this
        super.onCreate()
    }

    companion object {
        var instance: ApplicationContext? = null
            private set

        val context: Context?
            get() = instance
    }
}
