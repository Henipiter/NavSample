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
    override var fireStoreSync: Boolean = false
) : TranslateEntity {
    @PrimaryKey
    var id: String = ""

    override fun getEntityId(): String {
        return id
    }

    override fun toMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to this.id,
            "name" to this.name,
            "color" to this.color,
            "createdAt" to this.createdAt,
            "deletedAt" to this.deletedAt
        )
    }
}
