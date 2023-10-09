package com.example.navsample.DTO

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    var id: String?,
    var receiptId: String?,
    var name: String?,
    var finalPrice: String?,
    var category: String?,
    var amount: String?,
    var itemPrice: String?,
    var ptuType: String?
) : Parcelable {
    constructor() : this("---", "---", "---", "---", "---", "---", "---", "---")
}
