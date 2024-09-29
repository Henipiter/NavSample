package com.example.navsample.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["nip"], unique = true)])
data class Store(
    var nip: String, var name: String, var defaultCategoryId: Int
) : TranslateEntity {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

    override fun toMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to this.id,
            "nip" to this.nip,
            "name" to this.name,
            "defaultCategoryId" to this.defaultCategoryId
        )
    }

    override fun getDescriptiveId(): String {
        return "$id $name"
    }
}
