package com.example.navsample.entities

interface TranslateEntity {

    fun insertData(): HashMap<String, Any?>
    fun updateData(): HashMap<String, Any?>

    fun deleteData(): HashMap<String, Any?> {
        return hashMapOf(
            "deletedAt" to this.deletedAt
        )
    }

    fun synchronizeEntity(): HashMap<String, Any?> {
        return hashMapOf(
            "updatedAt" to this.updatedAt,
            "deletedAt" to this.deletedAt,
            "firestoreId" to this.firestoreId, //TODO DELETE
            "isSync" to this.isSync
        )
    }

    var createdAt: String
    var updatedAt: String
    var deletedAt: String
    var firestoreId: String
    var isSync: Boolean
    var upToDate: Boolean


}
