package com.example.navsample.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Product(
    var receiptId: String,
    var name: String,
    var categoryId: String,
    var quantity: Int,
    var unitPrice: Int,
    var subtotalPrice: Int,
    var discount: Int,
    var finalPrice: Int,
    var ptuType: String,
    var raw: String,
    var validPrice: Boolean,
    var createdAt: String = "",
    var deletedAt: String = ""
) : TranslateEntity {
    @PrimaryKey
    var id: String = ""

    override fun toMap(): HashMap<String, Any?> {
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
            "raw" to this.raw,
            "validPrice" to this.validPrice,
            "createdAt" to this.createdAt,
            "deletedAt" to this.deletedAt
        )
    }

    override fun getDescriptiveId(): String {
        return "$id $name"
    }
}
