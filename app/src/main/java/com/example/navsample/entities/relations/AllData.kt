package com.example.navsample.entities.relations

data class AllData(
    var storeName: String,
    var storeNip: String,
    var storeDefaultCategoryId: Int,

    var receiptPln: Double,
    var receiptPtu: Double,
    var receiptDate: String,
    var receiptTime: String,

    var productName: String,
    var productQuantity: Double,
    var productUnitPrice: Double,
    var productSubtotalPrice: Double,
    var productDiscount: Double,
    var productFinalPrice: Double,
    var productPtuType: String,
    var productRaw: String,

    var categoryName: String,
    var categoryColor: String,
)

