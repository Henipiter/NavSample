package com.example.navsample.entities.relations

data class ProductRichData(
    var storeName: String,

    var date: String,

    var categoryName: String,
    var categoryColor: String,

    var receiptId: Int,
    var name: String,
    var categoryId: Int,
    var amount: Float,
    var itemPrice: Float,
    var finalPrice: Float,
    var ptuType: String,
    var raw: String,
) {
}