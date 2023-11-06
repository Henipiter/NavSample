package com.example.navsample.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Receipt(
    var nip: String,
    var pln: Float,
    var ptu: Float,
    var date: String,
    var time: String
){
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

}