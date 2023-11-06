package com.example.navsample.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Store(
    @PrimaryKey(autoGenerate = false)
    var nip: String,
    var name: String
)