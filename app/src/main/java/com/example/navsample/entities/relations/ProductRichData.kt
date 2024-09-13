package com.example.navsample.entities.relations

data class ProductRichData(
    var storeId: Int,
    var storeName: String,

    var date: String,

    var categoryName: String,
    var categoryColor: String,

    var receiptId: Int,
    var name: String,
    var categoryId: Int,
    var quantity: Double,
    var unitPrice: Double,
    var subtotalPrice: Double,
    var discount: Double,
    var finalPrice: Double,
    var ptuType: String,
    var raw: String,
    var id: Int
)