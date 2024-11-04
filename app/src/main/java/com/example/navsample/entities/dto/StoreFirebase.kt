package com.example.navsample.entities.dto

class StoreFirebase(
    var id: String,
    var isCategorySync: Boolean,
    override var firestoreId: String,
    override var isSync: Boolean
) : TranslateFirebaseEntity
