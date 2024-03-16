package com.example.navsample.entities.relations


data class ReceiptWithStore(
    var id: Int,
    var storeId: Int,
    var nip: String,
    var name: String,
    var pln: Float,
    var ptu: Float,
    var date: String,
    var time: String,
    var productCount: Int
)