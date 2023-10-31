package com.example.navsample.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Receipt (
    @PrimaryKey
    var id:Int,
    var nip:String,
    var pln:Float,
    var ptu:Float
)