package com.example.navsample.DTO

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategoryDTO(
    var id: String?,
    var category: String?
) : Parcelable {
    constructor() : this("id",  "category")
}
