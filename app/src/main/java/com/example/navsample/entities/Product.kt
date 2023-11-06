package com.example.navsample.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Product(
    var receiptId: Int,
    var name: String,
    var categoryId: String,
    var amount: Float,
    var itemPrice: Float,
    var finalPrice: Float,
    var ptuType: String
){
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}