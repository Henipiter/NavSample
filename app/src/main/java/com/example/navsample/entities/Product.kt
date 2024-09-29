package com.example.navsample.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Product(
    var receiptId: Int,
    var name: String,
    var categoryId: Int,
    var quantity: Double,
    var unitPrice: Double,
    var subtotalPrice: Double,
    var discount: Double,
    var finalPrice: Double,
    var ptuType: String,
    var raw: String
){
    fun toMap(): HashMap<String, Any?> {
        return hashMapOf(
            "receiptId" to this.receiptId,
            "name" to this.name,
            "categoryId" to this.categoryId,
            "quantity" to this.quantity,
            "unitPrice" to this.unitPrice,
            "subtotalPrice" to this.subtotalPrice,
            "discount" to this.discount,
            "finalPrice" to this.finalPrice,
            "ptuType" to this.ptuType,
            "raw" to this.raw
        )
    }

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}