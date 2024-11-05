package com.example.navsample.entities.dto

interface TranslateFirebaseEntity {


    fun synchronizeEntity(): HashMap<String, Any?> {
        return hashMapOf(
            "firestoreId" to this.firestoreId, //TODO DELETE
            "isSync" to this.isSync
        )
    }

    var firestoreId: String
    var isSync: Boolean
    var toUpdate: Boolean
    var toDelete: Boolean


}
