package com.example.navsample.entities.dto

class ProductFirebase(
    var id: String,
    var isReceiptSync: Boolean,
    var isCategorySync: Boolean,
    override var firestoreId: String,
    override var isSync: Boolean,
    override var toUpdate: Boolean,
    override var toDelete: Boolean
) : TranslateFirebaseEntity
