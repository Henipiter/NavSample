package com.example.navsample.entities

interface TranslateEntity {
    fun toMap(): HashMap<String, Any?>
    fun getDescriptiveId(): String
}
