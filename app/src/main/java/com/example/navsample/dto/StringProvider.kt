package com.example.navsample.dto

import android.content.Context

class StringProvider(private var context: Context) {
    fun getString(resId: Int): String {
        return context.getString(resId)
    }
}