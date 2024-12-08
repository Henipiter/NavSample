package com.example.navsample.entities

object FirestoreHelperSingleton {
    private var firebaseHelper: FirebaseHelper? = null

    fun getInstance(): FirebaseHelper {
        if (firebaseHelper != null) {
            return firebaseHelper!!
        }
        throw Exception("FirebaseHelper not initialized")
    }

    fun initialize(userId: String) {
        firebaseHelper = FirebaseHelperFactory.build(userId)
    }
}