package com.example.navsample.entities

interface TranslateEntity {
    fun getEntityId(): String
    fun toMap(): HashMap<String, Any?>
    var createdAt: String
    var updatedAt: String
    var deletedAt: String
    var fireStoreSync: Boolean
}
