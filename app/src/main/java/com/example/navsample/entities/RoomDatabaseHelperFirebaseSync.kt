package com.example.navsample.entities

import android.util.Log
import com.example.navsample.entities.dto.CategoryFirebase
import com.example.navsample.entities.dto.ProductFirebase
import com.example.navsample.entities.dto.ReceiptFirebase
import com.example.navsample.entities.dto.StoreFirebase

class RoomDatabaseHelperFirebaseSync(
    private var dao: ReceiptDao
) {

    // GET ALL
    suspend fun getAllNotAddedCategories(): List<Category> {
        Log.i("Database", "getAllNotAddedCategories")
        return dao.getAllNotAddedCategories()
    }

    suspend fun getAllNotAddedStore(): List<Store> {
        Log.i("Database", "getAllNotAddedStore")
        return dao.getAllNotAddedStore()
    }

    suspend fun getAllNotAddedReceipt(): List<Receipt> {
        Log.i("Database", "getAllNotAddedReceipt")
        return dao.getAllNotAddedReceipt()
    }

    suspend fun getAllNotAddedProduct(): List<Product> {
        Log.i("Database", "getAllNotAddedProduct")
        return dao.getAllNotAddedProduct()
    }

    suspend fun getAllNotSyncedCategories(): List<CategoryFirebase> {
        Log.i("Database", "getAllNotSyncedCategories")
        return dao.getAllNotSyncedCategories()
    }

    suspend fun getAllNotSyncedStores(): List<StoreFirebase> {
        Log.i("Database", "getAllNotSyncedStores")
        return dao.getNotSyncedStoreForFirestore()
    }

    suspend fun getAllNotSyncedProducts(): List<ProductFirebase> {
        Log.i("Database", "getAllNotSyncedProducts")
        return dao.getNotSyncedProductForFirestore()
    }

    suspend fun getAllNotSyncedReceipts(): List<ReceiptFirebase> {
        Log.i("Database", "getAllNotSyncedReceipts")
        return dao.getNotSyncedReceiptForFirestore()
    }

    suspend fun getAllOutdatedCategories(): List<Category> {
        Log.i("Database", "getAllOutdatedCategories")
        return dao.getAllOutdatedCategories()
    }

    suspend fun getAllOutdatedStores(): List<Store> {
        Log.i("Database", "getAllOutdatedStores")
        return dao.getOutdatedStoreForFirestore()
    }

    suspend fun getAllOutdatedProducts(): List<Product> {
        Log.i("Database", "getAllOutdatedProducts")
        return dao.getOutdatedProductForFirestore()
    }

    suspend fun getAllOutdatedReceipts(): List<Receipt> {
        Log.i("Database", "getAllOutdatedReceipts")
        return dao.getOutdatedReceiptForFirestore()
    }

    suspend fun syncCategory(id: String) {
        Log.i("Database", "syncCategory")
        dao.syncCategory(id)
    }

    suspend fun syncStore(id: String) {
        Log.i("Database", "syncStore")
        dao.syncStore(id)
    }

    suspend fun syncReceipt(id: String) {
        Log.i("Database", "syncReceipt")
        dao.syncReceipt(id)
    }

    suspend fun syncProduct(id: String) {
        Log.i("Database", "syncProduct")
        dao.syncProduct(id)
    }

    suspend fun replaceCategoryWithDependencies(oldId: String) {
        dao.replaceCategoryWithDependencies(oldId)
    }

    suspend fun replaceStoreWithDependencies(oldId: String) {
        dao.replaceStoreWithDependencies(oldId)
    }

    suspend fun replaceReceiptWithDependencies(oldId: String) {
        return dao.replaceReceiptWithDependencies(oldId)
    }

    suspend fun replaceProductWithDependencies(oldId: String) {
        return dao.replaceProductWithDependencies(oldId)
    }
}
