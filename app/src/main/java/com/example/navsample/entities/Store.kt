package com.example.navsample.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["nip"], unique = true)])
data class Store(
    var name: String,
    var nip: String
){
    @PrimaryKey
    var id: Int? = null
}