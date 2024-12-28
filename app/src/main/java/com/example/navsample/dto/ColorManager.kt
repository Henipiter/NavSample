package com.example.navsample.dto

import android.content.Context
import android.graphics.Color
import android.util.Log
import com.example.navsample.R

class ColorManager {
    companion object {
        fun getNormalColor(context: Context): Int {
            return context.resources.getColor(R.color.basic_text_grey, context.theme)
        }

        fun getWrongColor(): Int {
            return Color.RED
        }

        fun parseColor(value: String): Int {
            return try {
                Color.parseColor(value)
            } catch (exception: Exception) {
                Log.e(TAG, "cannot parse category color: $value", exception)
                Color.WHITE
            }
        }

        const val TAG = "ColorManager"
    }

}