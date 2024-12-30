package com.example.navsample.entities.firestore

interface TranslateFirebaseEntity {


    fun getEntityId(): String
    fun synchronizeEntity(): HashMap<String, Any?>

    var firestoreId: String
    var isSync: Boolean
    var toUpdate: Boolean
    var toDelete: Boolean


}
