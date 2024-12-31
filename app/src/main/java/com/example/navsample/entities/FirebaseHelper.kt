package com.example.navsample.entities

import com.example.navsample.entities.firestore.TranslateFirebaseEntity
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlin.reflect.KClass

interface FirebaseHelper {
    fun <T : TranslateEntity> singleListener(
        objectClass: KClass<out T>,
        saveEntity: (T) -> Unit
    )

    fun <T : TranslateEntity> addFirestore(entity: T, addDocumentFunction: (String) -> Unit)
    fun <T : TranslateEntity> updateFirestore(entity: T, updateDb: (String) -> Unit)
    fun <T : TranslateEntity> updateFirestore(
        entity: T,
        data: HashMap<String, Any?>,
        updateDb: (String) -> Unit
    )

    fun <T : TranslateEntity> delete(entity: T, updateDb: (String) -> Unit)
    fun <T : TranslateFirebaseEntity> synchronize(entity: T, updateDb: (String) -> Unit)
    fun <T : TranslateEntity> delete(ids: List<T>, updateDb: (String) -> Unit)
    fun <T : TranslateEntity> getDataByQuery(type: KClass<out T>, date: String): Query?
    fun <T : TranslateEntity> convertQueryResponse(
        objectClass: KClass<out T>,
        snapshot: QuerySnapshot?
    ): List<T>
}