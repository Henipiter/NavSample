package com.example.navsample.entities.relations

import androidx.room.Ignore

data class ProductRichData(
    var storeId: Int,
    var storeName: String,
    var date: String,
    var categoryName: String,
    var categoryColor: String,
    var receiptId: Int,
    var name: String,
    var categoryId: Int,
    var quantity: Int,
    var unitPrice: Int,
    var subtotalPrice: Int,
    var discount: Int,
    var finalPrice: Int,
    var ptuType: String,
    var raw: String,
    var validPrice: Boolean,
    var id: Int,
    @Ignore
    var collapse: Boolean = true
) {
    constructor(
        storeId: Int,
        storeName: String,
        date: String,
        categoryName: String,
        categoryColor: String,
        receiptId: Int,
        name: String,
        categoryId: Int,
        quantity: Int,
        unitPrice: Int,
        subtotalPrice: Int,
        discount: Int,
        finalPrice: Int,
        ptuType: String,
        raw: String,
        validPrice: Boolean,
        id: Int,
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