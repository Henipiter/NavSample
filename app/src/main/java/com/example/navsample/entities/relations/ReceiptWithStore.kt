package com.example.navsample.entities.relations


data class ReceiptWithStore(
    var id: Int,
    var storeId: Int,
    var nip: String,
    var name: String,
    var defaultCategoryId: Int,
    var pln: Int,
    var ptu: Int,
    var date: String,
    var time: String,
    var validPriceSum: Boolean,
    var validProductCount: Int,
    var productCount: Int
)