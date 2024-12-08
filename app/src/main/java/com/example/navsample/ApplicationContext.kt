package com.example.navsample

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.example.navsample.entities.FirestoreHelperSingleton

class ApplicationContext : Application() {
    override fun onCreate() {
        instance = this
        super.onCreate()
        val userId = getSharedPreferences("preferences", AppCompatActivity.MODE_PRIVATE)
            .getString("userId", "") ?: ""
        FirestoreHelperSingleton.initialize(userId)
    }

    companion object {
        var instance: ApplicationContext? = null
            private set

        val context: Context?
            get() = instance
    }
}
