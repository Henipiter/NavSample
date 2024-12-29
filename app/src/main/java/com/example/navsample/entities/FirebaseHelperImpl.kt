package com.example.navsample.entities

import android.util.Log
import com.example.navsample.entities.dto.CategoryFirebase
import com.example.navsample.entities.dto.ProductFirebase
import com.example.navsample.entities.dto.ReceiptFirebase
import com.example.navsample.entities.dto.StoreFirebase
import com.example.navsample.entities.dto.TranslateFirebaseEntity
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import kotlin.reflect.KClass

class FirebaseHelperImpl(
    private var userUuid: String
) : FirebaseHelper {

    override fun <T : TranslateEntity> convertQueryResponse(
        objectClass: KClass<out T>,
        snapshot: QuerySnapshot?
    ): List<T> {
        val list = arrayListOf<T>()
        if (snapshot != null && !snapshot.metadata.isFromCache) {
            Log.d(
                "Firestore",
                "${objectClass.simpleName} docs size: ${snapshot.documents.size}"
            )
            for (document in snapshot.documents) {
                val entity = document.toObject(objectClass.java)
                entity?.let { list.add(it) }
            }
        } else {
            Log.d("Firestore", "Ignored local cache update")
        }
        return list
    }

    override fun <T : TranslateEntity> singleListener(
        objectClass: KClass<out T>,
        saveEntity: (T) -> Unit
    ) {
        getFullFirestorePath(objectClass)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, exception ->
                if (exception != null) {
                    Log.w("Firestore", "Listen failed.", exception)
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.metadata.isFromCache) {
                    Log.d(
                        "Firestore",
                        "${objectClass.simpleName} docs size: ${snapshot.documents.size}"
                    )
                    for (document in snapshot.documents) {
                        val entity = document.toObject(objectClass.java)
                        entity?.let {
                            saveEntity(entity)
                        }
                    }
                } else {
                    Log.d("Firestore", "Ignored local cache update")
                }
            }
    }


    override fun <T : TranslateEntity> getDataByQuery(type: KClass<out T>, date: String): Query {
        var query = getFullFirestorePath(type)
            .whereEqualTo("deletedAt", "")
            .orderBy("updatedAt")
            .limit(100)

        if (date != "") {
            query = query.whereGreaterThanOrEqualTo("updatedAt", date)
        }
        return query
    }

    override fun <T : TranslateEntity> addFirestore(
        entity: T,
        addDocumentFunction: (String) -> Unit
    ) {
        getFullFirestorePath(entity::class)
            .add(entity.insertData())
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

    override fun <T : TranslateEntity> updateFirestore(entity: T, updateDb: (String) -> Unit) {
        updateFirestore(entity, entity.updateData(), updateDb)
    }

    override fun <T : TranslateEntity> updateFirestore(
        entity: T,
        data: HashMap<String, Any?>,
        updateDb: (String) -> Unit
    ) {
        getFullFirestorePath(entity::class)
            .document(entity.firestoreId)
            .update(data)
            .addOnSuccessListener {
                updateDb.invoke(entity.getEntityId())
                Log.i("Firebase", "Updating entity ${entity::class} end successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error updating entity ${entity::class}: ${e.message}")
            }
    }

    override fun <T : TranslateEntity> delete(entity: T, updateDb: (String) -> Unit) {
        if (entity.firestoreId != "") {
            getFullFirestorePath(entity::class)
                .document(entity.firestoreId)
                .update(entity.deleteData())
                .addOnSuccessListener {
                    updateDb.invoke(entity.getEntityId())
                    Log.i(
                        "Firebase",
                        "Field deletedAt was successfully updated for '${entity.javaClass.simpleName}'."
                    )
                }
                .addOnFailureListener { e ->
                    Log.e(
                        "Firebase",
                        "Updating field deletedAt for '${entity.javaClass.simpleName} error: ${e.message}"
                    )
                }
        }
    }

    override fun <T : TranslateFirebaseEntity> synchronize(entity: T, updateDb: (String) -> Unit) {
        if (entity.firestoreId != "") {
            getFullFirestorePath(entity::class)
                .document(entity.firestoreId)
                .update(entity.synchronizeEntity())
                .addOnSuccessListener {
                    updateDb.invoke(entity.getEntityId())
                    Log.i(
                        "Firebase",
                        "Field sync for '${entity::class.simpleName}' was successfully updated."
                    )
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Sync error for '${entity::class.simpleName}': ${e.message}")
                }
        }
    }

    override fun <T : TranslateEntity> delete(ids: List<T>, updateDb: (String) -> Unit) {
        ids.forEach { entity ->
            delete(entity, updateDb)
        }
    }

    private fun getFullFirestorePath(type: KClass<out Any>): CollectionReference {
        return getFirestoreUserPath(getPath(type))
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

            Category::class, CategoryFirebase::class -> {
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

        private const val COLLECTION_PATH = "userTest2"
        private const val PRODUCT_FIRESTORE_PATH = "products"
        private const val RECEIPT_FIRESTORE_PATH = "receipts"
        private const val STORE_FIRESTORE_PATH = "stores"
        private const val CATEGORY_FIRESTORE_PATH = "categories"
    }
}
