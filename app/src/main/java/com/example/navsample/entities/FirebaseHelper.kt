package com.example.navsample.entities

import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore

class FirebaseHelper(
    private var userUuid: String
) {
    fun <T : TranslateEntity> addFirestore(obj: T) {
        getFirestoreUserPath(getPath(obj)).document(obj.getDescriptiveId()).set(obj)
    }

    fun <T : TranslateEntity> updateFirestore(obj: T) {
        getFirestoreUserPath(getPath(obj))
            .document(obj.getDescriptiveId())
            .update(obj.toMap())
    }

    private fun getFirestoreUserPath(entityPath: String): CollectionReference {
        return Firebase.firestore.collection(COLLECTION_PATH).document(userUuid)
            .collection(entityPath)

    }

    private fun getPath(obj: Any): String {
        when (obj) {
            is Store -> {
                return STORE_FIRESTORE_PATH
            }

            is Category -> {
                return CATEGORY_FIRESTORE_PATH
            }

            is Receipt -> {
                return RECEIPT_FIRESTORE_PATH
            }

            is Product -> {
                return PRODUCT_FIRESTORE_PATH
            }
        }
        return "null"

    }


    companion object {
        private const val COLLECTION_PATH = "user"
        private const val PRODUCT_FIRESTORE_PATH = "products"
        private const val RECEIPT_FIRESTORE_PATH = "receipts"
        private const val STORE_FIRESTORE_PATH = "stores"
        private const val CATEGORY_FIRESTORE_PATH = "categories"
    }
}
