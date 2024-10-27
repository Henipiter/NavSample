package com.example.navsample.entities

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore
import kotlin.reflect.KClass

class FirebaseHelper(
    private var userUuid: String
) {

    fun <T : TranslateEntity> addFirestore(obj: T, addDocumentFunction: (String) -> Unit) {
        getFirestoreUserPath(getPath(obj::class)).add(obj)
            .addOnSuccessListener { documentReference ->
                Log.i(
                    "Firestore",
                    "Entity document ${obj::class} with id ${obj.getEntityId()} wass added with id: ${documentReference.id}"
                )
                addDocumentFunction.invoke(documentReference.id)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error while adding document: $e")
            }
    }

    fun <T : TranslateEntity> updateFirestore(obj: T) {
        getFirestoreUserPath(getPath(obj::class))
            .document(obj.getEntityId())
            .update(obj.toMap())
            .addOnSuccessListener {
                Log.i("Firebase", "Updating entity ${obj::class} end successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error updating entity ${obj::class}: ${e.message}")
            }
    }

    fun <T : TranslateEntity> delete(entity: T) {
        if (entity.fireStoreSync) {
            getFirestoreUserPath(getPath(entity::class))
                .document(entity.getEntityId())
                .update("deletedAt", entity.deletedAt)
                .addOnSuccessListener {
                    Log.i("Firebase", "Field deletedAt was successfully updated.")
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Updating field deletedAt error: ${e.message}")
                }
        }
    }

    fun <T : TranslateEntity> delete(ids: List<T>) {
        ids.forEach { entity ->
            delete(entity)
        }
    }

    private fun getFirestoreUserPath(entityPath: String): CollectionReference {
        return Firebase.firestore.collection(COLLECTION_PATH).document(userUuid)
            .collection(entityPath)

    }

    private fun getPath(type: KClass<out TranslateEntity>): String {
        when (type) {
            Store::class -> {
                return STORE_FIRESTORE_PATH
            }

            Category::class -> {
                return CATEGORY_FIRESTORE_PATH
            }

            Receipt::class -> {
                return RECEIPT_FIRESTORE_PATH
            }

            Product::class -> {
                return PRODUCT_FIRESTORE_PATH
            }
        }
        return "null"
    }

    companion object {

        private const val COLLECTION_PATH = "userTest"
        private const val PRODUCT_FIRESTORE_PATH = "products"
        private const val RECEIPT_FIRESTORE_PATH = "receipts"
        private const val STORE_FIRESTORE_PATH = "stores"
        private const val CATEGORY_FIRESTORE_PATH = "categories"
    }
}
