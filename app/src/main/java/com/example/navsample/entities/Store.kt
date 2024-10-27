package com.example.navsample.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["nip"], unique = true)])
data class Store(
    var nip: String,
    var name: String,
    var defaultCategoryId: String,
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
            "nip" to this.nip,
            "name" to this.name,
            "defaultCategoryId" to this.defaultCategoryId,
            "createdAt" to this.createdAt,
            "deletedAt" to this.deletedAt
        )
    }
}
