package com.example.navsample.entities.dto

class ProductFirebase(
    var id: String,
    var isReceiptSync: Boolean,
    override var firestoreId: String,
    override var isSync: Boolean
) : TranslateFirebaseEntity
