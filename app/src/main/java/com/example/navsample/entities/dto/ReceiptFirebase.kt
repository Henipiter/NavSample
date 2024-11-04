package com.example.navsample.entities.dto

class ReceiptFirebase(
    var id: String,
    var isStoreSync: Boolean,
    override var firestoreId: String,
    override var isSync: Boolean
) : TranslateFirebaseEntity
