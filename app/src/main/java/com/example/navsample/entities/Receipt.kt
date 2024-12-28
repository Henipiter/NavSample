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
    override var createdAt: String = "",
    override var updatedAt: String = "",
    override var deletedAt: String = "",
    override var firestoreId: String = "",
    override var isSync: Boolean = false,
    override var toUpdate: Boolean = false,
    override var toDelete: Boolean = false
) : TranslateEntity {
    @PrimaryKey
    var id: String = ""

    constructor() : this("", -1, -1, "", "")
    override fun insertData(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to this.id,
            "storeId" to this.storeId,
            "pln" to this.pln,
            "ptu" to this.ptu,
            "date" to this.date,
            "time" to this.time,
            "createdAt" to this.createdAt,
            "updatedAt" to this.updatedAt,
            "deletedAt" to this.deletedAt,
            "firestoreId" to this.firestoreId, //TODO DELETE
            "isSync" to this.isSync
        )
    }

    override fun updateData(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to this.id,
            "storeId" to this.storeId,
            "pln" to this.pln,
            "ptu" to this.ptu,
            "date" to this.date,
            "time" to this.time,
            "updatedAt" to this.updatedAt
        )
    }

    override fun getEntityId(): String {
        return id
    }
}