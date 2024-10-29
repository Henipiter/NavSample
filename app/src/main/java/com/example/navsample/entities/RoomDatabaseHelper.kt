package com.example.navsample.entities

import android.util.Log
import com.example.navsample.dto.DateUtil
import com.example.navsample.dto.sort.ReceiptWithStoreSort
import com.example.navsample.dto.sort.RichProductSort
import com.example.navsample.dto.sort.SortProperty
import com.example.navsample.dto.sort.StoreSort
import com.example.navsample.entities.dto.StoreFirebase
import com.example.navsample.entities.relations.AllData
import com.example.navsample.entities.relations.PriceByCategory
import com.example.navsample.entities.relations.ProductRichData
import com.example.navsample.entities.relations.ReceiptWithStore
import com.example.navsample.entities.relations.TableCounts
import java.util.UUID

class RoomDatabaseHelper(
    private var dao: ReceiptDao
) {

    // GET ALL
    suspend fun getTableCounts(): List<TableCounts> {
        Log.i("Database", "Get table counts")
        return dao.getTableCounts()
    }

    suspend fun getAllData(): List<AllData> {
        Log.i("Database", "Get all data")
        return dao.getAllData()
    }

    suspend fun getAllCategories(): List<Category> {
        Log.i("Database", "Refresh category list")
        return dao.getAllCategories()
    }

    suspend fun getAllStores(): List<Store> {
        Log.i("Database", "Refresh store list")
        return dao.getAllStores()
    }

    //GET CHART
    suspend fun getPricesForCategoryComparisonWithDate(
        dateFrom: String,
        dateTo: String
    ): List<PriceByCategory> {
        Log.i("Database", "Get prices grouped by category and date")
        return dao.getPricesForCategoryComparisonWithDate(dateFrom, dateTo)
    }

    suspend fun getPricesForCategoryComparison(
        dateFrom: String,
        dateTo: String
    ): List<PriceByCategory> {
        Log.i("Database", "Get prices grouped by category")
        return dao.getPricesForCategoryComparison(dateFrom, dateTo)
    }

    //GET ALL SORTED
    suspend fun getAllCategories(categoryName: String): List<Category> {
        Log.i("Database", "Refresh category list filtered by name '$categoryName'")
        return dao.getAllCategories(categoryName)
    }

    suspend fun getAllStoresOrdered(
        storeName: String, nip: String, sort: SortProperty<StoreSort>
    ): List<Store> {
        Log.i(
            "Database",
            "Refresh store list filtered by: storeName '$storeName', nip '$nip', sort '$sort'"
        )
        val query = QueryDaoHelper.getAllStoresOrdered(storeName, nip, sort)
        return dao.getAllStoresOrdered(query)
    }

    suspend fun getReceiptWithStoreOrdered(
        storeName: String,
        dateFrom: String,
        dateTo: String,
        sort: SortProperty<ReceiptWithStoreSort>
    ): List<ReceiptWithStore> {
        Log.i(
            "Database",
            "Refresh receipts filtered by:  storeName '$storeName', dateFrom '$dateFrom', dateTo '$dateTo', sort '$sort'"
        )
        val query = QueryDaoHelper.getReceiptWithStore(
            storeName,
            if (dateFrom == "") "0" else dateFrom,
            if (dateTo == "") "9" else dateTo,
            sort
        )
        return dao.getReceiptWithStoreOrdered(query)
    }

    suspend fun getAllProductsOrderedWithHigherPrice(
        storeName: String,
        categoryName: String,
        dateFrom: String,
        dateTo: String,
        lowerPrice: Int,
        higherPrice: Int,
        sort: SortProperty<RichProductSort>
    ): List<ProductRichData> {
        Log.i(
            "Database",
            "Refresh products filtered by:  storeName '$storeName', categoryName '$categoryName', dateFrom '$dateFrom', dateTo '$dateTo', lowerPrice '$lowerPrice', higherPrice '$higherPrice', sort '$sort'"
        )
        val query = QueryDaoHelper.getAllProductsOrdered(
            storeName,
            categoryName,
            if (dateFrom == "") "0" else dateFrom,
            if (dateTo == "") "9" else dateTo,
            lowerPrice,
            higherPrice,
            sort
        )
        return dao.getAllProductsOrderedWithHigherPrice(query)
    }

    suspend fun getAllProductsOrderedWithHigherPrice(
        sort: SortProperty<RichProductSort>
    ): List<ProductRichData> {
        Log.i(
            "Database",
            "Refresh products filtered by: sort '$sort'"
        )
        val query = QueryDaoHelper.getAllProductsOrdered(
            "", "", "0", "9", 0, -1, sort
        )
        return dao.getAllProductsOrderedWithHigherPrice(query)
    }

    //GET BY FIRESTORE ID
    suspend fun getStoreForFirestore(id: String): StoreFirebase? {
        Log.i("Database", "Get store by id '$id'")
        return dao.getStoreForFirestore(id)
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

    // UPDATE
    suspend fun updateCategory(category: Category, update: Boolean = true): Category {
        Log.i("Database", "Update category '${category.name}' with id '${category.id}'")
        if (update) {
            category.updatedAt = DateUtil.getCurrentUtcTime()
        }
        dao.updateCategory(category)
        return category
    }

    suspend fun updateProduct(product: Product, update: Boolean = true): Product {
        Log.i("Database", "Update product '${product.name}' with id '${product.id}'")
        if (update) {
            product.updatedAt = DateUtil.getCurrentUtcTime()
        }
        dao.updateProduct(product)
        return product
    }

    suspend fun updateStore(store: Store, update: Boolean = true): Store {
        Log.i("Database", "Update store '${store.name}' with id '${store.id}'")
        if (update) {
            store.updatedAt = DateUtil.getCurrentUtcTime()
        }
        dao.updateStore(store)
        return store
    }

    suspend fun updateReceipt(receipt: Receipt, update: Boolean = true): Receipt {
        Log.i(
            "Database",
            "Update receipt from ${receipt.date} payed ${receipt.pln} with id '${receipt.id}'"
        )
        if (update) {
            receipt.updatedAt = DateUtil.getCurrentUtcTime()
        }
        dao.updateReceipt(receipt)
        return receipt
    }
//UPDATE FOREIGN KEY

    suspend fun updateCategoryIdInStore(
        oldCategoryId: String,
        newCategoryId: String,
        firebaseUpdate: (Store) -> Unit
    ) {
        val stores = dao.getAllStoresWithCategoryId(oldCategoryId)
        stores.forEach {
            if (it.id == it.firestoreId) {
                it.isSync = true
            }
            it.defaultCategoryId = newCategoryId
            it.updatedAt = DateUtil.getCurrentUtcTime()
            updateStore(it)
            if (it.firestoreId != "") {
                firebaseUpdate.invoke(it)
            }
        }
    }

    suspend fun updateStoreIdInReceipt(
        oldStoreId: String,
        newStoreId: String,
        firebaseUpdate: (Receipt) -> Unit
    ) {
        val receipts = dao.getAllReceiptWithStoreId(oldStoreId)
        receipts.forEach {
            if (it.id == it.firestoreId) {
                it.isSync = true
            }
            it.storeId = newStoreId
            it.updatedAt = DateUtil.getCurrentUtcTime()
            updateReceipt(it)
            if (it.firestoreId != "") {
                firebaseUpdate.invoke(it)
            }
        }
    }

    // UPDATE ENTITY ID
    suspend fun updateStoreId(store: Store) {
        store.id = store.firestoreId
        dao.updateStore(store)

    }

    // IS SYNC
    suspend fun isCategorySynced(categoryId: String): Boolean {
        return dao.isCategorySynced(categoryId)
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
