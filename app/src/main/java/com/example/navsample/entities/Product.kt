package com.example.navsample.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Product (
    @PrimaryKey
    var id:Int,
    var receiptId: Int,
    var name:String,
    var categoryId: Int,
    var amount:Float,
    var itemPrice:Float,
    var finalPrice:Float,
    var ptuType:String
)