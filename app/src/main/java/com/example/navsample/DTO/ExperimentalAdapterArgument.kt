package com.example.navsample.DTO

import android.graphics.Color

data class ExperimentalAdapterArgument(
    var value: String,
    var color: Int
) {
    constructor(value: String) : this(value, Color.GRAY)
    constructor() : this("", Color.GRAY)
}