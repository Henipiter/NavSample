package com.example.navsample.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["name"], unique = true)])
data class Category(
    var name: String,
    var color: String,
    override var createdAt: String = "",
    override var updatedAt: String = "",
    override var deletedAt: String = "",
    override var firestoreId: String = "",
    override var isSync: Boolean = false,
    override var upToDate: Boolean = false
) : TranslateEntity {
    @PrimaryKey
    var id: String = ""

    override fun insertData(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to this.id,
            "name" to this.name,
            "color" to this.color,
            "createdAt" to this.createdAt,
            "updatedAt" to this.updatedAt,
            "deletedAt" to this.deletedAt,
            "firestoreId" to this.firestoreId, //TODO DELETE
            "isSync" to this.isSync
        )
    }

    override fun updateData(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to this.id,
            "name" to this.name,
            "color" to this.color,
            "updatedAt" to this.updatedAt
        )
    }
}
