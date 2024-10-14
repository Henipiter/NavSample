package com.example.navsample.entities.relations


data class ReceiptWithStore(
    var id: Int,
    var storeId: Int,
    var nip: String,
    var name: String,
    var defaultCategoryId: Int,
    var pln: Double,
    var ptu: Double,
    var date: String,
    var time: String,
    var validProductCount: Int,
    var productCount: Int
)