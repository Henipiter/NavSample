package com.example.navsample.DTO

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductDTO(
    var id: Int?,
    var receiptId: Int,
    var name: String?,
    var finalPrice: String?,
    var category: String?,
    var color: String?,
    var amount: String?,
    var itemPrice: String?,
    var ptuType: String?,
    var original: String?,
) : Parcelable {
    constructor() : this(
        -1,
        -1,
        "---",
        "---",
        ChartColors.DEFAULT_CATEGORY_COLOR_STRING,
        "---",
        "---",
        "---",
        "---",
        "---"
    )
}
