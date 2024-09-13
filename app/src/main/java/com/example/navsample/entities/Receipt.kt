package com.example.navsample.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Receipt(
    var storeId: Int,
    var pln: Double,
    var ptu: Double,
    var date: String,
    var time: String
){
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}