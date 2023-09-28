package com.example.navsample.DTO

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Store(
    var id: String?,
    var store: String?
) : Parcelable {
    constructor() : this("id",  "store")
}
