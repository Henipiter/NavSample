package com.example.navsample.DTO

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    var id: String?,
    var name: String?,
    var price: Float,
    var category: String?,
    var receiptId: String?
) : Parcelable {
    constructor() : this("id", "name", 0F, "category", "receiptId")
}
