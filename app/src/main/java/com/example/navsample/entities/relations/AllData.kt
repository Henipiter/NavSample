package com.example.navsample.entities.relations

data class AllData(
    var storeName: String,
    var storeNip: String,

    var receiptPln: Float,
    var receiptPtu: Float,
    var receiptDate: String,
    var receiptTime: String,

    var productName: String,
    var productAmount: Float,
    var productItemPrice: Float,
    var productFinalPrice: Float,
    var productPtuType: String,
    var productRaw: String,

    var categoryName: String,
    var categoryColor: String,
)

