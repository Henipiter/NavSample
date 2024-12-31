package com.example.navsample.entities.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.navsample.entities.TranslateEntity

@Entity(primaryKeys = ["productId", "tagId"])
data class ProductTagCrossRef(
    var productId: String,
    var tagId: String,
    override var createdAt: String = "",
    override var updatedAt: String = "",
    override var deletedAt: String = "",
    override var firestoreId: String = "",
    override var isSync: Boolean = false,
    override var toUpdate: Boolean = false,
    override var toDelete: Boolean = false
) : TranslateEntity {
    @PrimaryKey
    var id: String = ""

    constructor() : this("", "")

    override fun insertData(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to this.id,
            "productId" to this.productId,
            "tagId" to this.tagId,
            "createdAt" to this.createdAt,
            "updatedAt" to this.updatedAt,
            "deletedAt" to this.deletedAt,
            "firestoreId" to this.firestoreId, //TODO DELETE
            "isSync" to this.isSync
        )
    }

    override fun updateData(): HashMap<String, Any?> {
        return hashMapOf(
            "productId" to this.productId,
            "tagId" to this.tagId,
            "updatedAt" to this.updatedAt
        )
    }

    override fun getEntityId(): String {
        return id
    }
}
