package com.example.navsample.entities

import android.util.Log
import com.example.navsample.entities.dto.ProductFirebase
import com.example.navsample.entities.dto.ReceiptFirebase
import com.example.navsample.entities.dto.StoreFirebase
import com.example.navsample.entities.dto.TranslateFirebaseEntity
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore
import kotlin.reflect.KClass

class FirebaseHelper(
    private var userUuid: String
) {

    fun <T : TranslateEntity> addFirestore(entity: T, addDocumentFunction: (String) -> Unit) {
        getFirestoreUserPath(getPath(entity::class)).add(entity.insertData())
            .addOnSuccessListener { documentReference ->
                Log.i(
                    "Firestore",
                    "Entity document ${entity::class} with id ${entity.firestoreId} was added with id: ${documentReference.id}"
                )
                addDocumentFunction.invoke(documentReference.id)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error while adding document: $e")
            }
    }

    fun <T : TranslateEntity> updateFirestore(entity: T) {
        getFirestoreUserPath(getPath(entity::class))
            .document(entity.firestoreId)
            .update(entity.updateData())
            .addOnSuccessListener {
                Log.i("Firebase", "Updating entity ${entity::class} end successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error updating entity ${entity::class}: ${e.message}")
            }
    }

    fun <T : TranslateEntity> delete(entity: T) {
        if (entity.firestoreId != "") {
            getFirestoreUserPath(getPath(entity::class))
                .document(entity.firestoreId)
                .update(entity.deleteData())
                .addOnSuccessListener {
                    Log.i("Firebase", "Field deletedAt was successfully updated.")
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Updating field deletedAt error: ${e.message}")
                }
        }
    }

    fun <T : TranslateFirebaseEntity> synchronize(entity: T) {
        synchronize(entity.firestoreId, entity::class, entity.synchronizeEntity())

    }

    fun <T : TranslateEntity> synchronize(entity: T) {
        synchronize(entity.firestoreId, entity::class, entity.synchronizeEntity())
    }

    private fun synchronize(
        firestoreId: String,
        entity: KClass<out Any>,
        synchronizeData: HashMap<String, Any?>
    ) {
        if (firestoreId != "") {
            getFirestoreUserPath(getPath(entity))
                .document(firestoreId)
                .update(synchronizeData)
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

    private fun getPath(type: KClass<out Any>): String {
        when (type) {
            Store::class, StoreFirebase::class -> {
                return STORE_FIRESTORE_PATH
            }

            Category::class -> {
                return CATEGORY_FIRESTORE_PATH
            }

            Receipt::class, ReceiptFirebase::class -> {
                return RECEIPT_FIRESTORE_PATH
            }

            Product::class, ProductFirebase::class -> {
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
