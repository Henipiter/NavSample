package com.example.navsample.entities.relations


data class ReceiptWithStore(
    var id: String,
    var storeId: String,
    var nip: String,
    var name: String,
    var defaultCategoryId: String,
    var pln: Int,
    var ptu: Int,
    var date: String,
    var time: String,
    var productPriceSum: Int,
    var validProductCount: Int,
    var productCount: Int,
    var isSync: Boolean,
    var toUpdate: Boolean,
    var toDelete: Boolean
)