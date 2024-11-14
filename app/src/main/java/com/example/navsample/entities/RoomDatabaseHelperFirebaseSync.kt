package com.example.navsample.entities

import android.util.Log
import com.example.navsample.entities.dto.ProductFirebase
import com.example.navsample.entities.dto.ReceiptFirebase
import com.example.navsample.entities.dto.StoreFirebase

class RoomDatabaseHelperFirebaseSync(
    private var dao: ReceiptDao
) {

    // GET ALL

    suspend fun getAllNotSyncedCategories(): List<Category> {
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

    suspend fun syncCategory(id: String, updatedAt: String) {
        Log.i("Database", "Refresh store list")
        dao.syncCategory(id, updatedAt)
    }

    suspend fun syncStore(id: String, updatedAt: String) {
        Log.i("Database", "Refresh store list")
        dao.syncStore(id, updatedAt)
    }

    suspend fun syncReceipt(id: String, updatedAt: String) {
        Log.i("Database", "Refresh store list")
        dao.syncReceipt(id, updatedAt)
    }

    suspend fun syncProduct(id: String, updatedAt: String) {
        Log.i("Database", "Refresh store list")
        dao.syncProduct(id, updatedAt)
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
}
