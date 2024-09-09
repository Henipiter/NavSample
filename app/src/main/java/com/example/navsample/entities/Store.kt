package com.example.navsample.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["nip"], unique = true)])
data class Store(
    var nip: String,
    var name: String,
    var defaultCategoryId: Int
){
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}