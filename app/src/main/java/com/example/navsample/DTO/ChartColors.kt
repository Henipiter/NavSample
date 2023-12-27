package com.example.navsample.DTO

import android.graphics.Color

class ChartColors {


    companion object {
        val COLORS = arrayOf(
            "#FF0000", "#00FF00", "#0000FF", "#800080", "#008080",
            "#FF4500", "#008000", "#008000", "#FF6347", "#1E90FF",
            "#FFD700", "#DA70D6", "#00FF7F", "#FFB6C0", "#4B0082",
            "#FF8C00"
        )
        const val DEFAULT_CATEGORY_COLOR_STRING = "#BBBBBB"
        val DEFAULT_CATEGORY_COLOR_INT = Color.rgb(187, 187, 187)
    }
}
