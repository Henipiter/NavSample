package com.example.navsample.entities.relations

import androidx.room.Ignore

data class ProductRichData(
    var storeId: String,
    var storeName: String,
    var date: String,
    var categoryName: String,
    var categoryColor: String,
    var receiptId: String,
    var name: String,
    var categoryId: String,
    var quantity: Int,
    var unitPrice: Int,
    var subtotalPrice: Int,
    var discount: Int,
    var finalPrice: Int,
    var ptuType: String,
    var raw: String,
    var validPrice: Boolean,
    var id: String,
    @Ignore
    var collapse: Boolean = true
) {
    constructor(
        storeId: String,
        storeName: String,
        date: String,
        categoryName: String,
        categoryColor: String,
        receiptId: String,
        name: String,
        categoryId: String,
        quantity: Int,
        unitPrice: Int,
        subtotalPrice: Int,
        discount: Int,
        finalPrice: Int,
        ptuType: String,
        raw: String,
        validPrice: Boolean,
        id: String,
    ) : this(
        storeId,
        storeName,
        date,
        categoryName,
        categoryColor,
        receiptId,
        name,
        categoryId,
        quantity,
        unitPrice,
        subtotalPrice,
        discount,
        finalPrice,
        ptuType,
        raw,
        validPrice,
        id,
        true
    )
}