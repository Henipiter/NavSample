package com.example.navsample.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Product(
    var receiptId: Int,
    var name: String,
    var categoryId: Int,
    var quantity: Float,
    var unitPrice: Float,
    var subtotalPrice: Float,
    var ptuType: String,
    var raw: String
){
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}