package com.example.navsample.entities

class FirebaseHelperFactory {
    companion object {
        fun build(userUuid: String): FirebaseHelper {
            return if (userUuid != "") {
                FirebaseHelperImpl(userUuid)
            } else {
                FirebaseHelperImplEmpty()
            }
        }
    }
}
