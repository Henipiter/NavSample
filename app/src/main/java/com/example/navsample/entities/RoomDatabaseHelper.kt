package com.example.navsample.entities

import android.util.Log
import com.example.navsample.dto.sort.ReceiptWithStoreSort
import com.example.navsample.dto.sort.RichProductSort
import com.example.navsample.dto.sort.SortProperty
import com.example.navsample.dto.sort.StoreSort
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
    suspend fun insertCategory(category: Category): Category {
        category.id = UUID.randomUUID().toString()
        Log.i("Database", "Insert category '${category.name}' with id '${category.id}'")
        dao.insertCategory(category)
        return category
    }

    suspend fun insertProduct(product: Product): Product {
        product.id = UUID.randomUUID().toString()
        Log.i("Database", "Insert product '${product.name}' with id '${product.id}'")
        dao.insertProduct(product)
        return product
    }

    suspend fun insertReceipt(receipt: Receipt): Receipt {
        receipt.date = convertDateFormat(receipt.date)
        receipt.id = UUID.randomUUID().toString()
        Log.i("Database", "Insert receipt with id '${receipt.id}'")
        dao.insertReceipt(receipt)
        return receipt
    }

    suspend fun insertStore(store: Store): Store {
        store.id = UUID.randomUUID().toString()
        Log.i("Database", "Insert store with id '${store.id}'")
        dao.insertStore(store)
        return store
    }

    // UPDATE
    suspend fun updateCategory(category: Category) {
        Log.i("Database", "Update category '${category.name}' with id '${category.id}'")
        dao.updateCategory(category)
    }

    suspend fun updateProduct(product: Product) {
        Log.i("Database", "Update product '${product.name}' with id '${product.id}'")
        dao.updateProduct(product)
    }

    suspend fun updateStore(store: Store) {
        Log.i("Database", "Update store '${store.name}' with id '${store.id}'")
        dao.updateStore(store)
    }

    suspend fun updateReceipt(receipt: Receipt) {
        Log.i(
            "Database",
            "Update receipt from ${receipt.date} payed ${receipt.pln} with id '${receipt.id}'"
        )
        dao.updateReceipt(receipt)
    }

    // DELETE
    suspend fun deleteCategory(category: Category) {
        Log.i("Database", "Delete category '${category.name}' with id '${category.id}'")
        dao.deleteCategory(category)
    }

    suspend fun deleteReceipt(receiptId: String) {
        Log.i("Database", "Delete receipt with id '$receiptId'")
        dao.deleteProductsOfReceipt(receiptId)
        dao.deleteReceiptById(receiptId)
    }

    suspend fun deleteStore(storeId: String) {
        Log.i("Database", "Delete store with id '$storeId'")
        dao.deleteProductsOfStore(storeId)
        dao.deleteReceiptsOfStore(storeId)
        dao.deleteStoreById(storeId)
    }

    suspend fun deleteProductById(productId: String) {
        Log.i("Database", "Delete product with id '$productId'")
        dao.deleteProductById(productId)
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
