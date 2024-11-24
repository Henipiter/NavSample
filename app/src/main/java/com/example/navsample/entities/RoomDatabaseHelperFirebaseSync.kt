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
    suspend fun getAllNotSyncedCategories(): List<CategoryFirebase> {
        Log.i("Database", "Refresh category list")
        return dao.getAllNotSyncedCategories()
    }

    suspend fun getAllNotSyncedStores(): List<StoreFirebase> {
        Log.i("Database", "Refresh store list")
        return dao.getNotSyncedStoreForFirestore()
    }

    suspend fun getAllNotSyncedProducts(): List<ProductFirebase> {
        Log.i("Database", "Refresh store list")
        return dao.getNotSyncedProductForFirestore()
    }

    suspend fun getAllNotSyncedReceipts(): List<ReceiptFirebase> {
        Log.i("Database", "Refresh store list")
        return dao.getNotSyncedReceiptForFirestore()
    }

    suspend fun getAllOutdatedCategories(): List<Category> {
        Log.i("Database", "Refresh category list")
        return dao.getAllOutdatedCategories()
    }

    suspend fun getAllOutdatedStores(): List<Store> {
        Log.i("Database", "Refresh store list")
        return dao.getOutdatedStoreForFirestore()
    }

    suspend fun getAllOutdatedProducts(): List<Product> {
        Log.i("Database", "Refresh store list")
        return dao.getOutdatedProductForFirestore()
    }

    suspend fun getAllOutdatedReceipts(): List<Receipt> {
        Log.i("Database", "Refresh store list")
        return dao.getOutdatedReceiptForFirestore()
    }

    suspend fun syncCategory(id: String) {
        Log.i("Database", "Refresh store list")
        dao.syncCategory(id)
    }

    suspend fun syncStore(id: String) {
        Log.i("Database", "Refresh store list")
        dao.syncStore(id)
    }

    suspend fun syncReceipt(id: String) {
        Log.i("Database", "Refresh store list")
        dao.syncReceipt(id)
    }

    suspend fun syncProduct(id: String) {
        Log.i("Database", "Refresh store list")
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
