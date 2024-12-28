package com.example.navsample.entities

interface TranslateEntity {

    fun getEntityId(): String
    fun insertData(): HashMap<String, Any?>
    fun updateData(): HashMap<String, Any?>

    fun deleteData(): HashMap<String, Any?> {
        return hashMapOf(
            "updatedAt" to this.updatedAt,
            "deletedAt" to this.deletedAt
        )
    }

    var createdAt: String
    var updatedAt: String
    var deletedAt: String
    var firestoreId: String
    var isSync: Boolean
    var toUpdate: Boolean
    var toDelete: Boolean


}
