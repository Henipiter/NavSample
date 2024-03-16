package com.example.navsample.entities.relations

data class ProductRichData(
    var storeName: String,

    var date: String,

    var categoryName: String,
    var categoryColor: String,

    var receiptId: Int,
    var name: String,
    var categoryId: Int,
    var quantity: Float,
    var unitPrice: Float,
    var subtotalPrice: Float,
    var ptuType: String,
    var raw: String,
    var id: Int
)