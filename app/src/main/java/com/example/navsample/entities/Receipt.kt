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
) : TranslateEntity {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

    override fun toMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to this.id,
            "storeId" to this.storeId,
            "pln" to this.pln,
            "ptu" to this.ptu,
            "date" to this.date,
            "time" to this.time
        )
    }

    override fun getDescriptiveId(): String {
        return "$id $date $time"
    }
}