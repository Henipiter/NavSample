package com.example.navsample.entities.dto

interface TranslateFirebaseEntity {


    fun synchronizeEntity(): HashMap<String, Any?>

    var firestoreId: String
    var isSync: Boolean
    var toUpdate: Boolean
    var toDelete: Boolean


}
