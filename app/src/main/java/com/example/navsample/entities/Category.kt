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
}
