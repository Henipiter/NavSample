package com.example.navsample.entities.relations


data class ReceiptWithStore(
    var id: Int,
    var storeId: Int,
    var name: String,
    var pln: Float,
    var ptu: Float,
    var date: String,
    var time: String
)