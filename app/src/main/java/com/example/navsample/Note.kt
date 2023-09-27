package com.example.navsample

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Note(
    var id: String?,
    var price: String?,
    var productName: String?
) : Parcelable
