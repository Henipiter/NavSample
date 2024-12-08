package com.example.navsample.entities

import com.example.navsample.entities.dto.TranslateFirebaseEntity
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

}