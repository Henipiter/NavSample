package com.example.navsample.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["name"], unique = true)])
data class Category(
    var name: String,
    var color: String,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

    fun toMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to this.id,
            "name" to this.name,
            "color" to this.color
        )
    }
}
