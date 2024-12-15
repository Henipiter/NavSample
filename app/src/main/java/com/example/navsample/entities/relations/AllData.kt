package com.example.navsample.entities.relations

data class AllData(
    var storeName: String,
    var storeNip: String,
    var storeDefaultCategoryId: String,

    var receiptPln: Int,
    var receiptPtu: Int,
    var receiptDate: String,
    var receiptTime: String,
    var receiptValidPrice: Boolean,

    var productName: String,
    var productQuantity: Int,
    var productUnitPrice: Int,
    var productSubtotalPrice: Int,
    var productDiscount: Int,
    var productFinalPrice: Int,
    var productPtuType: String,
    var productRaw: String,
    var productValidPrice: Boolean,

    var categoryName: String,
    var categoryColor: String,
)

