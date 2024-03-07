package com.example.navsample.DTO

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReceiptDTO(
    var id: Int?,
    var storeName: String?,
    var storeNIP: String?,
    var receiptPLN: Float?,
    var receiptPTU: Float?,
    var receiptDate: String?,
    var receiptTime: String?,
) : Parcelable {
    constructor() : this(-1, "---", "---", -1f, -1f, "---", "---")
}
