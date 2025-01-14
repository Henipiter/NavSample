package com.example.navsample.entities.converters

import com.example.navsample.entities.TranslateEntity
import com.example.navsample.entities.database.Category
import com.example.navsample.entities.database.Store
import com.google.firebase.firestore.DocumentSnapshot
import kotlin.reflect.KClass

class DocumentToEntityConverter {

    companion object {

        fun <T : TranslateEntity> convert(
            document: DocumentSnapshot,
            objectClass: KClass<out T>
        ): T? {
            return when (objectClass) {
                Category::class -> convertCategory(document) as T
                Store::class -> convertStore(document) as T
                else -> null
            }
        }

        private fun convertCategory(document: DocumentSnapshot): Category {
            return Category().apply {
                this.id = document.id
                this.name = document.get("name") as String
                this.color = document.get("color") as String
                this.createdAt = document.get("createdAt") as String
                this.updatedAt = document.get("updatedAt") as String
                this.deletedAt = document.get("deletedAt") as String
                this.firestoreId = document.get("firestoreId") as String
                this.isSync = document.get("isSync") as Boolean
            }

        }

        private fun convertStore(document: DocumentSnapshot): Store {
            return Store().apply {
                this.id = document.id
                this.nip = document.get("nip") as String
                this.name = document.get("name") as String
                this.defaultCategoryId = document.get("defaultCategoryId") as String
                this.createdAt = document.get("createdAt") as String
                this.updatedAt = document.get("updatedAt") as String
                this.deletedAt = document.get("deletedAt") as String
                this.firestoreId = document.get("firestoreId") as String
                this.isSync = document.get("isSync") as Boolean
            }

        }
    }

}