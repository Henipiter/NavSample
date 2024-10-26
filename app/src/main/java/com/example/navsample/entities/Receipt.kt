package com.example.navsample.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Receipt(
    var storeId: String,
    var pln: Int,
    var ptu: Int,
    var date: String,
    var time: String,
    var validPrice: Boolean,
    var createdAt: String = "",
    var deletedAt: String = ""
) : TranslateEntity {
    @PrimaryKey
    var id: String = ""

    override fun toMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to this.id,
            "storeId" to this.storeId,
            "pln" to this.pln,
            "ptu" to this.ptu,
            "date" to this.date,
            "time" to this.time,
            "createdAt" to this.createdAt,
            "deletedAt" to this.deletedAt
        )
    }

    override fun getDescriptiveId(): String {
        return "$id $date $time"
    }
}