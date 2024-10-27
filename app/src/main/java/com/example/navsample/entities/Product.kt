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
    override var createdAt: String = "",
    override var updatedAt: String = "",
    override var deletedAt: String = "",
    override var fireStoreSync: Boolean = false
) : TranslateEntity {
    @PrimaryKey
    var id: String = ""

    override fun getEntityId(): String {
        return id
    }

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
}
