package com.example.navsample.entities.database

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.navsample.entities.TranslateEntity

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
    override var firestoreId: String = "",
    override var isSync: Boolean = false,
    override var toUpdate: Boolean = false,
    override var toDelete: Boolean = false,
    @Ignore var tagList: List<Tag> = listOf(),
    @Ignore var originalTagList: List<Tag> = listOf()
) : TranslateEntity {

    @PrimaryKey
    var id: String = ""

    constructor() : this("", "", "", -1, -1, -1, -1, -1, "", "", false)

    override fun insertData(): HashMap<String, Any?> {
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
            "updatedAt" to this.updatedAt,
            "deletedAt" to this.deletedAt,
            "firestoreId" to this.firestoreId, //TODO DELETE
            "isSync" to this.isSync
        )
    }

    override fun updateData(): HashMap<String, Any?> {
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
            "validPrice" to this.validPrice
        )
    }

    override fun getEntityId(): String {
        return id
    }
}
