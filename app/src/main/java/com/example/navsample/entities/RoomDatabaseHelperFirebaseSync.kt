package com.example.navsample.entities

import android.util.Log
import com.example.navsample.dto.DateUtil
import com.example.navsample.entities.dto.ProductFirebase
import com.example.navsample.entities.dto.ReceiptFirebase
import com.example.navsample.entities.dto.StoreFirebase
import java.util.UUID

class RoomDatabaseHelperFirebaseSync(
    private var dao: ReceiptDao
) {

    // GET ALL

    suspend fun getAllNotSyncedCategories(): List<Category> {
        Log.i("Database", "Refresh category list")
        return dao.getAllNotSyncedCategories()
    }

    suspend fun getAllNotSyncedStores(): List<Store> {
        Log.i("Database", "Refresh store list")
        return dao.getAllNotSyncedStores()
    }

    suspend fun getAllNotSyncedProducts(): List<Product> {
        Log.i("Database", "Refresh store list")
        return dao.getAllNotSyncedProducts()
    }

    suspend fun getAllNotSyncedReceipts(): List<Receipt> {
        Log.i("Database", "Refresh store list")
        return dao.getAllNotSyncedReceipts()
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

    suspend fun getStoreForFirestore(): List<StoreFirebase> {
        return dao.getStoreForFirestore()
    }

    suspend fun getReceiptForFirestore(): List<ReceiptFirebase> {
        return dao.getReceiptForFirestore()
    }

    suspend fun getProductForFirestore(): List<ProductFirebase> {
        return dao.getProductForFirestore()
    }

    suspend fun replaceCategoryWithDependencies(oldId: String, newCategory: Category) {
        return dao.replaceCategoryWithDependencies(oldId, newCategory)
    }

    suspend fun getAllStoresWithCategory(categoryId: String): List<Store> {
        return dao.getAllStoresWithCategory(categoryId)
    }


    // GET BY ID
    suspend fun getCategoryById(id: String): Category {
        Log.i("Database", "Get category by id '$id'")
        return dao.getCategoryById(id)
    }

    suspend fun getReceiptById(id: String): Receipt {
        Log.i("Database", "Get receipt by id '$id'")
        return dao.getReceiptById(id)
    }

    suspend fun getStoreById(id: String): Store {
        Log.i("Database", "Get store by id '$id'")
        return dao.getStoreById(id)
    }

    suspend fun getProductById(id: String): Product {
        Log.i("Database", "Get product by id '$id'")
        return dao.getProductById(id)
    }

    suspend fun getProductsByReceiptId(receiptId: String): List<Product> {
        Log.i("Database", "Get products by receipt id '$receiptId'")
        return dao.getAllProducts(receiptId)
    }

    // INSERT
    suspend fun insertCategory(category: Category, generateId: Boolean = true): Category {
        if (generateId) {
            category.id = UUID.randomUUID().toString()
        }
        val timestamp = DateUtil.getCurrentUtcTime()
        category.createdAt = timestamp
        category.updatedAt = timestamp
        Log.i("Database", "Insert category '${category.name}' with id '${category.id}'")
        dao.insertCategory(category)
        return category
    }

    suspend fun insertProduct(product: Product, generateId: Boolean = true): Product {
        if (generateId) {
            product.id = UUID.randomUUID().toString()
        }
        val timestamp = DateUtil.getCurrentUtcTime()
        product.createdAt = timestamp
        product.updatedAt = timestamp
        Log.i("Database", "Insert product '${product.name}' with id '${product.id}'")
        dao.insertProduct(product)
        return product
    }

    suspend fun insertReceipt(receipt: Receipt, generateId: Boolean = true): Receipt {
        receipt.date = convertDateFormat(receipt.date)
        if (generateId) {
            receipt.id = UUID.randomUUID().toString()
        }
        val timestamp = DateUtil.getCurrentUtcTime()
        receipt.createdAt = timestamp
        receipt.updatedAt = timestamp
        Log.i("Database", "Insert receipt with id '${receipt.id}'")
        dao.insertReceipt(receipt)
        return receipt
    }

    suspend fun insertStore(store: Store, generateId: Boolean = true): Store {
        if (generateId) {
            store.id = UUID.randomUUID().toString()
        }
        val timestamp = DateUtil.getCurrentUtcTime()
        store.createdAt = timestamp
        store.updatedAt = timestamp
        Log.i("Database", "Insert store with id '${store.id}'")
        dao.insertStore(store)

        return store
    }


    // DELETE
    suspend fun deleteCategory(categoryId: String): Category {
        Log.i("Database", "Delete category with id '${categoryId}'")
        val deletedAt = DateUtil.getCurrentUtcTime()
        return dao.deleteAndSelectCategoryById(categoryId, deletedAt)
    }

    suspend fun deleteReceiptProducts(receiptId: String): List<Product> {
        Log.i("Database", "Delete receipt with id '$receiptId'")
        val deletedAt = DateUtil.getCurrentUtcTime()
        return dao.deleteAndSelectProductsOfReceipt(receiptId, deletedAt)
    }

    suspend fun deleteReceipt(receiptId: String): Receipt {
        Log.i("Database", "Delete receipt with id '$receiptId'")
        val deletedAt = DateUtil.getCurrentUtcTime()
        return dao.deleteAndSelectReceiptById(receiptId, deletedAt)
    }

    suspend fun deleteStoreReceipts(storeId: String): List<Receipt> {
        Log.i("Database", "Delete store receipts with storeId '$storeId'")
        val deletedAt = DateUtil.getCurrentUtcTime()
        return dao.deleteAndSelectReceiptsOfStore(storeId, deletedAt)
    }

    suspend fun deleteStoreProducts(storeId: String): List<Product> {
        Log.i("Database", "Delete store products with storeId '$storeId'")
        val deletedAt = DateUtil.getCurrentUtcTime()
        return dao.deleteAndSelectProductsOfStore(storeId, deletedAt)
    }

    suspend fun deleteStore(storeId: String): Store {
        Log.i("Database", "Delete store with storeId '$storeId'")
        val deletedAt = DateUtil.getCurrentUtcTime()
        return dao.deleteAndSelectStore(storeId, deletedAt)
    }

    suspend fun deleteProductById(productId: String): Product {
        Log.i("Database", "Delete product with id '$productId'")
        val deletedAt = DateUtil.getCurrentUtcTime()
        return dao.deleteAndSelectProductById(productId, deletedAt)
    }


    private fun convertDateFormat(date: String): String {
        val newDate = date.replace(".", "-")
        val splitDate = newDate.split("-")
        try {
            if (splitDate[2].length == 4) {
                return splitDate[2] + "-" + splitDate[1] + "-" + splitDate[0]
            }
            return newDate
        } catch (e: Exception) {
            Log.e("ConvertDate", "Cannot convert date: $splitDate")
            return newDate
        }
    }
}
