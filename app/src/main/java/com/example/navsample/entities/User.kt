package com.example.navsample.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    var uuid: String?
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}
