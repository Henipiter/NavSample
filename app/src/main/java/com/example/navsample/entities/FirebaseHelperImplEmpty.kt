package com.example.navsample.entities

import android.util.Log
import com.example.navsample.entities.dto.TranslateFirebaseEntity
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlin.reflect.KClass

class FirebaseHelperImplEmpty : FirebaseHelper {
    override fun <T : TranslateEntity> singleListener(
        objectClass: KClass<out T>,
        saveEntity: (T) -> Unit
    ) {
        Log.d("Firestore", "singleListener - firestore inactive")
    }

    override fun <T : TranslateEntity> addFirestore(
        entity: T,
        addDocumentFunction: (String) -> Unit
    ) {
        Log.d("Firestore", "addFirestore - firestore inactive")
    }

    override fun <T : TranslateEntity> updateFirestore(entity: T, updateDb: (String) -> Unit) {
        Log.d("Firestore", "updateFirestore - firestore inactive")
    }

    override fun <T : TranslateEntity> updateFirestore(
        entity: T,
        data: HashMap<String, Any?>,
        updateDb: (String) -> Unit
    ) {
        Log.d("Firestore", "updateFirestore - firestore inactive")
    }

    override fun <T : TranslateEntity> delete(entity: T, updateDb: (String) -> Unit) {
        Log.d("Firestore", "delete - firestore inactive")
    }

    override fun <T : TranslateFirebaseEntity> synchronize(entity: T, updateDb: (String) -> Unit) {
        Log.d("Firestore", "synchronize - firestore inactive")
    }

    override fun <T : TranslateEntity> getDataByQuery(type: KClass<out T>, date: String): Query? {
        Log.d("Firestore", "getDataByQuery - firestore inactive")
        return null
    }

    override fun <T : TranslateEntity> convertQueryResponse(
        objectClass: KClass<out T>,
        snapshot: QuerySnapshot?
    ): List<T> {
        return emptyList()
    }

    override fun <T : TranslateEntity> delete(ids: List<T>, updateDb: (String) -> Unit) {
        Log.d("Firestore", "delete - firestore inactive")
    }

}
