package com.example.navsample.entities.converters

import android.util.Log
import com.example.navsample.entities.TranslateEntity
import com.example.navsample.entities.database.Category
import com.example.navsample.entities.database.Product
import com.example.navsample.entities.database.ProductTagCrossRef
import com.example.navsample.entities.database.Receipt
import com.example.navsample.entities.database.Store
import com.example.navsample.entities.database.Tag
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
                Product::class -> convertProduct(document) as T
                Receipt::class -> convertReceipt(document) as T
                Tag::class -> convertTag(document) as T
                ProductTagCrossRef::class -> convertProductTag(document) as T
                else -> {
                    Log.w("Convert", "Unknown entity - cannot convert")
                    null
                }
            }
        }

        private fun convertProduct(document: DocumentSnapshot): Product {
            return Product().apply {
                this.id = document.id
                this.receiptId = document.get("receiptId") as String
                this.name = document.get("name") as String
                this.categoryId = document.get("categoryId") as String
                this.quantity = (document.get("quantity") as Long).toInt()
                this.unitPrice = (document.get("unitPrice") as Long).toInt()
                this.subtotalPrice = (document.get("subtotalPrice") as Long).toInt()
                this.discount = (document.get("discount") as Long).toInt()
                this.finalPrice = (document.get("finalPrice") as Long).toInt()
                this.ptuType = document.get("ptuType") as String
                this.raw = document.get("raw") as String
                this.validPrice = document.get("validPrice") as Boolean

                this.createdAt = document.get("createdAt") as String
                this.updatedAt = document.get("updatedAt") as String
                this.deletedAt = document.get("deletedAt") as String
                this.firestoreId = document.get("firestoreId") as String
                this.isSync = document.get("isSync") as Boolean
            }

        }

        private fun convertReceipt(document: DocumentSnapshot): Receipt {
            return Receipt().apply {
                this.id = document.id
                this.storeId = document.get("storeId") as String
                this.pln = (document.get("pln") as Long).toInt()
                this.ptu = (document.get("ptu") as Long).toInt()
                this.date = document.get("date") as String
                this.time = document.get("time") as String

                this.createdAt = document.get("createdAt") as String
                this.updatedAt = document.get("updatedAt") as String
                this.deletedAt = document.get("deletedAt") as String
                this.firestoreId = document.get("firestoreId") as String
                this.isSync = document.get("isSync") as Boolean
            }

        }

        private fun convertProductTag(document: DocumentSnapshot): ProductTagCrossRef {
            return ProductTagCrossRef().apply {
                this.id = document.id
                this.productId = document.get("productId") as String
                this.tagId = document.get("tagId") as String

                this.createdAt = document.get("createdAt") as String
                this.updatedAt = document.get("updatedAt") as String
                this.deletedAt = document.get("deletedAt") as String
                this.firestoreId = document.get("firestoreId") as String
                this.isSync = document.get("isSync") as Boolean
            }

        }

        private fun convertTag(document: DocumentSnapshot): Tag {
            return Tag().apply {
                this.id = document.id
                this.name = document.get("name") as String

                this.createdAt = document.get("createdAt") as String
                this.updatedAt = document.get("updatedAt") as String
                this.deletedAt = document.get("deletedAt") as String
                this.firestoreId = document.get("firestoreId") as String
                this.isSync = document.get("isSync") as Boolean
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