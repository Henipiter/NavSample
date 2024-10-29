package com.example.navsample.entities

interface TranslateEntity {
    fun toMap(): HashMap<String, Any?>
    var createdAt: String
    var updatedAt: String
    var deletedAt: String
    var firestoreId: String
    var isSync: Boolean
}
