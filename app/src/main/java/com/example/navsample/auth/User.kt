package com.example.navsample.auth

data class User(
    val id: String = "",
    val email: String = "",
    val provider: String = "",
    val displayName: String = ""
)