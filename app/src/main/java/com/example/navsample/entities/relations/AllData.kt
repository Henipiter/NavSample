package com.example.navsample.entities.relations

data class AllData(
    var storeName: String,
    var storeNip: String,

    var receiptPln: Float,
    var receiptPtu: Float,
    var receiptDate: String,
    var receiptTime: String,

    var productName: String,
    var productQuantity: Float,
    var productUnitPrice: Float,
    var productSubtotalPrice: Float,
    var productPtuType: String,
    var productRaw: String,

    var categoryName: String,
    var categoryColor: String,
)

