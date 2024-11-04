package com.example.navsample.entities

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.navsample.dto.DateUtil
import com.example.navsample.entities.dto.ProductFirebase
import com.example.navsample.entities.dto.ReceiptFirebase
import com.example.navsample.entities.dto.StoreFirebase
import com.example.navsample.entities.relations.AllData
import com.example.navsample.entities.relations.PriceByCategory
import com.example.navsample.entities.relations.ProductRichData
import com.example.navsample.entities.relations.ReceiptWithStore
import com.example.navsample.entities.relations.TableCounts

@Dao
interface ReceiptDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: User): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStore(store: Store)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: Receipt)

    @Update
    suspend fun updateReceipt(receipt: Receipt)

    @Update
    suspend fun updateProduct(product: Product)

    @Update
    suspend fun updateCategory(category: Category)


    @Query(
        "UPDATE category " +
                "SET color = :color, " +
                "name = :name, " +
                "updatedAt = :updatedAt " +
                "WHERE id = :id"
    )
    suspend fun updateCategoryFields(id: String, name: String, color: String, updatedAt: String)

    @Query(
        "UPDATE store " +
                "SET nip = :nip, " +
                "name = :name, " +
                "defaultCategoryId= :defaultCategoryId, " +
                "updatedAt = :updatedAt " +
                "WHERE id = :id"
    )
    suspend fun updateStoreFields(
        id: String,
        name: String,
        nip: String,
        defaultCategoryId: String,
        updatedAt: String
    )

    @Query(
        "UPDATE receipt " +
                "SET date = :date, " +
                "time = :time, " +
                "pln = :pln, " +
                "ptu = :ptu, " +
                "storeId = :storeId, " +
                "updatedAt = :updatedAt " +
                "WHERE id = :id"
    )
    suspend fun updateReceiptFields(
        id: String,
        date: String,
        time: String,
        pln: Int,
        ptu: Int,
        storeId: String,
        updatedAt: String
    )

    @Query(
        "UPDATE product " +
                "SET name = :name, " +
                "categoryId = :categoryId, " +
                "quantity = :quantity, " +
                "unitPrice = :unitPrice, " +
                "subtotalPrice = :subtotalPrice, " +
                "discount = :discount, " +
                "finalPrice = :finalPrice, " +
                "raw = :raw, " +
                "ptuType = :ptuType, " +
                "validPrice = :validPrice, " +
                "updatedAt = :updatedAt " +
                "WHERE id = :id"
    )
    suspend fun updateProductFields(
        id: String,
        name: String,
        categoryId: String,
        quantity: Int,
        unitPrice: Int,
        subtotalPrice: Int,
        discount: Int,
        finalPrice: Int,
        raw: String,
        ptuType: String,
        validPrice: Boolean,
        updatedAt: String
    )

    @Query("UPDATE category SET firestoreId = :firestoreId WHERE id = :id")
    suspend fun updateCategoryFirestoreId(id: String, firestoreId: String)

    @Query("UPDATE store SET firestoreId = :firestoreId WHERE id = :id")
    suspend fun updateStoreFirestoreId(id: String, firestoreId: String)

    @Query("UPDATE receipt SET firestoreId = :firestoreId WHERE id = :id")
    suspend fun updateReceiptFirestoreId(id: String, firestoreId: String)

    @Query("UPDATE product SET firestoreId = :firestoreId WHERE id = :id")
    suspend fun updateProductFirestoreId(id: String, firestoreId: String)

    @Query("UPDATE category SET deletedAt = :deletedAt WHERE id = :id")
    suspend fun deleteCategoryById(id: String, deletedAt: String)

    @Query("SELECT * FROM category WHERE deletedAt = :deletedAt AND id = :id")
    suspend fun selectDeletedCategoryById(id: String, deletedAt: String): Category

    @Transaction
    suspend fun deleteAndSelectCategoryById(id: String, deletedAt: String): Category {
        deleteCategoryById(id, deletedAt)
        return selectDeletedCategoryById(id, deletedAt)
    }

    @Query("UPDATE store SET deletedAt = :deletedAt WHERE id = :id")
    suspend fun deleteStoreById(id: String, deletedAt: String)

    @Query("SELECT * FROM store WHERE deletedAt = :deletedAt AND id = :id")
    suspend fun selectDeletedStoreById(id: String, deletedAt: String): Store

    suspend fun deleteAndSelectStore(id: String, deletedAt: String): Store {
        deleteStoreById(id, deletedAt)
        return selectDeletedStoreById(id, deletedAt)
    }

    @Query("UPDATE product SET deletedAt = :deletedAt WHERE id = :id")
    suspend fun deleteProductById(id: String, deletedAt: String)

    @Query("SELECT * FROM product WHERE deletedAt = :deletedAt AND id = :id")
    suspend fun selectDeletedProductById(id: String, deletedAt: String): Product

    @Transaction
    suspend fun deleteAndSelectProductById(id: String, deletedAt: String): Product {
        deleteProductById(id, deletedAt)
        return selectDeletedProductById(id, deletedAt)
    }

    @Query("UPDATE receipt SET deletedAt = :deletedAt WHERE id = :id")
    suspend fun deleteReceiptById(id: String, deletedAt: String)

    @Query("SELECT * FROM receipt WHERE deletedAt = :deletedAt AND id = :id")
    suspend fun selectDeletedReceiptById(id: String, deletedAt: String): Receipt

    @Transaction
    suspend fun deleteAndSelectReceiptById(id: String, deletedAt: String): Receipt {
        deleteReceiptById(id, deletedAt)
        return selectDeletedReceiptById(id, deletedAt)
    }

    @Query(
        "UPDATE product SET deletedAt = :deletedAt WHERE id IN (" +
                "SELECT p.id FROM product p, receipt r " +
                "WHERE p.receiptId = r.id AND r.id = :id)"
    )
    suspend fun deleteProductsOfReceipt(id: String, deletedAt: String)

    @Query(
        "SELECT * FROM product WHERE deletedAt = :deletedAt AND id IN (" +
                "SELECT p.id FROM product p, receipt r " +
                "WHERE p.receiptId = r.id AND r.id = :id)"
    )
    suspend fun selectDeletedProductsOfReceipt(id: String, deletedAt: String): List<Product>

    @Transaction
    suspend fun deleteAndSelectProductsOfReceipt(id: String, deletedAt: String): List<Product> {
        deleteProductsOfReceipt(id, deletedAt)
        return selectDeletedProductsOfReceipt(id, deletedAt)
    }

    @Query(
        "UPDATE receipt SET deletedAt = :deletedAt WHERE id IN (" +
                "SELECT r.id FROM receipt r, store s " +
                "WHERE s.id = r.storeId AND s.id = :id" +
                ")"
    )
    suspend fun deleteReceiptsOfStore(id: String, deletedAt: String)

    @Query(
        "SELECT * FROM receipt WHERE deletedAt = :deletedAt AND id IN (" +
                "SELECT r.id FROM receipt r, store s " +
                "WHERE s.id = r.storeId AND s.id = :id" +
                ")"
    )
    suspend fun selectDeletedReceiptsOfStore(id: String, deletedAt: String): List<Receipt>

    @Transaction
    suspend fun deleteAndSelectReceiptsOfStore(id: String, deletedAt: String): List<Receipt> {
        deleteReceiptsOfStore(id, deletedAt)
        return selectDeletedReceiptsOfStore(id, deletedAt)
    }

    @Query(
        "UPDATE product SET deletedAt = :deletedAt WHERE id IN (" +
                "SELECT p.id FROM product p, receipt r, store s " +
                "WHERE p.receiptId = r.id AND s.id = r.storeId AND s.id = :id " +
                ")"
    )
    suspend fun deleteProductsOfStore(id: String, deletedAt: String)

    @Query(
        "SELECT * FROM product WHERE deletedAt = :deletedAt AND id IN (" +
                "SELECT p.id FROM product p, receipt r, store s " +
                "WHERE p.receiptId = r.id AND s.id = r.storeId AND s.id = :id " +
                ")"
    )
    suspend fun selectProductsOfStore(id: String, deletedAt: String): List<Product>

    @Transaction
    suspend fun deleteAndSelectProductsOfStore(id: String, deletedAt: String): List<Product> {
        deleteProductsOfStore(id, deletedAt)
        return selectProductsOfStore(id, deletedAt)
    }


    @Update
    suspend fun updateStore(store: Store)

    @Query("SELECT * FROM receipt WHERE id = :id AND deletedAt == ''")
    suspend fun getReceipt(id: String): Receipt

    @Query("SELECT * FROM category WHERE id = :id AND deletedAt == ''")
    suspend fun getCategoryById(id: String): Category?

    @Query("SELECT uuid FROM user WHERE id = 0")
    suspend fun getUserUuid(): String?

    @Query("SELECT * FROM store WHERE nip = :nip AND deletedAt == ''")
    suspend fun getStoreByNip(nip: String): Store

    @Query("SELECT * FROM store WHERE id = :id AND deletedAt == ''")
    suspend fun getStoreById(id: String): Store?

    @Query("SELECT * FROM product WHERE id = :id AND deletedAt == ''")
    suspend fun getProductById(id: String): Product?

    @Query("SELECT * FROM receipt WHERE id = :id AND deletedAt == ''")
    suspend fun getReceiptById(id: String): Receipt?

    @Query("SELECT * FROM store WHERE deletedAt == '' ORDER BY name")
    suspend fun getAllStores(): List<Store>

    @Query("SELECT * FROM store WHERE deletedAt == '' AND defaultCategoryId = :categoryId ORDER BY name")
    suspend fun getAllStoresWithCategory(categoryId: String): List<Store>

    @Query("SELECT * FROM category WHERE isSync == 0")
    suspend fun getAllNotSyncedCategories(): List<Category>

    @Query("SELECT * FROM store WHERE isSync == 0")
    suspend fun getAllNotSyncedStores(): List<Store>

    @Query("SELECT * FROM product WHERE isSync == 0")
    suspend fun getAllNotSyncedProducts(): List<Product>

    @Query("SELECT * FROM receipt WHERE isSync == 0")
    suspend fun getAllNotSyncedReceipts(): List<Receipt>


    @Query("UPDATE category SET isSync = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun syncCategory(id: String, updatedAt: String)

    @Query("UPDATE store SET isSync = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun syncStore(id: String, updatedAt: String)

    @Query("UPDATE receipt SET isSync = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun syncReceipt(id: String, updatedAt: String)

    @Query("UPDATE product SET isSync = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun syncProduct(id: String, updatedAt: String)

    @Query(
        "SELECT  s.id,s.firestoreId,s.isSync,  c.isSync as isCategorySync " +
                "FROM store s INNER JOIN category c ON s.defaultCategoryId = c.id " +
                "WHERE s.isSync = 0"
    )
    suspend fun getNotSyncedStoreForFirestore(): List<StoreFirebase>

    @Query(
        "SELECT  r.id,r.firestoreId,r.isSync,  s.isSync as isStoreSync " +
                "FROM receipt r INNER JOIN store s ON r.storeId = s.id " +
                "WHERE r.isSync = 0"
    )
    suspend fun getNotSyncedReceiptForFirestore(): List<ReceiptFirebase>

    @Query(
        "SELECT  p.id,p.firestoreId,p.isSync,  r.isSync as isReceiptSync " +
                "FROM product p INNER JOIN receipt r ON p.receiptId = r.id " +
                "WHERE p.isSync = 0"
    )
    suspend fun getNotSyncedProductForFirestore(): List<ProductFirebase>


    @RawQuery
    suspend fun getAllStoresOrdered(query: SupportSQLiteQuery): List<Store>

    @RawQuery
    suspend fun getReceiptWithStoreOrdered(query: SupportSQLiteQuery): List<ReceiptWithStore>

    @Query("SELECT * FROM category WHERE deletedAt == '' ORDER BY name")
    suspend fun getAllCategories(): List<Category>

    @Query("SELECT * FROM category WHERE name LIKE '%'||:name||'%' AND deletedAt == '' ORDER BY name")
    suspend fun getAllCategories(name: String): List<Category>

    @Query("SELECT * FROM product WHERE receiptId=:receiptId AND deletedAt == ''")
    suspend fun getAllProducts(receiptId: String): List<Product>

    @RawQuery
    suspend fun getAllProductsOrderedWithHigherPrice(query: SupportSQLiteQuery): List<ProductRichData>

    @Query(
        "SELECT 'store' AS 'tableName', count(*) AS 'count' FROM store WHERE deletedAt == '' " +
                "UNION ALL SELECT 'product' AS 'tableName', count(*)  AS 'count' FROM product WHERE deletedAt == '' " +
                "UNION ALL SELECT 'category' AS 'tableName', count(*) AS 'count' FROM category WHERE deletedAt == '' " +
                "UNION ALL SELECT 'receipt' AS 'tableName', count(*) AS 'count' FROM receipt WHERE deletedAt == '' "
    )
    suspend fun getTableCounts(): List<TableCounts>

    //charts
    @Query(
        "SELECT sum(p.finalPrice) AS price, c.name AS category, substr(r.date,0,8) AS date " +
                "FROM product p, receipt r, category c " +
                "WHERE p.receiptId = r.id AND p.categoryId = c.id " +
                "AND date>=:dateFrom AND date<=:dateTo " +
                "AND p.deletedAt == '' AND r.deletedAt == '' AND c.deletedAt == '' " +
                "GROUP BY categoryId, substr(r.date,0,8) " +
                "ORDER BY date, pln DESC"
    )
    suspend fun getPricesForCategoryComparisonWithDate(
        dateFrom: String = "0",
        dateTo: String = "9",
    ): List<PriceByCategory>

    @Query(
        "SELECT sum(p.finalPrice) AS price, c.name AS category, '' AS date " +
                "FROM product p, receipt r, category c " +
                "WHERE p.receiptId = r.id AND p.categoryId = c.id " +
                "AND  date>=:dateFrom AND date<=:dateTo " +
                "AND p.deletedAt == '' AND c.deletedAt == '' " +
                "GROUP BY categoryId " +
                "ORDER BY pln DESC"
    )
    suspend fun getPricesForCategoryComparison(
        dateFrom: String = "0",
        dateTo: String = "9",
    ): List<PriceByCategory>

    @Query(
        "SELECT s.name AS storeName, s.nip AS storeNip, s.defaultCategoryId AS storeDefaultCategoryId, " +
                "r.pln AS receiptPln, r.ptu AS receiptPtu, " +
                "r.date AS receiptDate, r.time AS receiptTime, " +
                "p.name AS productName, p.quantity AS productQuantity, " +
                "p.unitPrice AS productUnitPrice, p.subtotalPrice AS productSubtotalPrice, " +
                "p.discount AS productDiscount, p.finalPrice AS productFinalPrice, " +
                "p.ptuType AS productPtuType, p.raw AS productRaw, " +
                "p.validPrice AS productValidPrice, " +
                "c.name AS categoryName, c.color AS categoryColor " +
                "FROM product p, receipt r, store s, category c " +
                "WHERE p.receiptId = r.id AND s.id =r.storeId AND p.categoryId = c.id " +
                "AND p.deletedAt == '' AND r.deletedAt == '' AND c.deletedAt == '' AND s.deletedAt == '' "
    )
    suspend fun getAllData(): List<AllData>


    @Transaction
    suspend fun replaceCategoryWithDependencies(oldId: String) {
        Log.d("Firestore", "Current category id: $oldId")
        val category = getCategoryById(oldId)
        Log.d("Firestore", "Current category: $category")
        if (category != null) {
            deleteCategoryById(oldId)
            category.id = category.firestoreId
            category.updatedAt = DateUtil.getCurrentUtcTime()
            insertCategory(category)
            updateDependentStoreByCategoryId(oldId, category.id, category.updatedAt)
        } else {
            Log.d("Firestore", "Current category id not found: $oldId")
        }
    }

    @Transaction
    suspend fun replaceStoreWithDependencies(oldId: String) {
        val store = getStoreById(oldId)
        if (store != null) {
            deleteStoreById(oldId)
            store.id = store.firestoreId
            store.updatedAt = DateUtil.getCurrentUtcTime()
            insertStore(store)
            updateDependentReceiptByStoreId(oldId, store.id, store.updatedAt)
        }
    }

    @Transaction
    suspend fun replaceReceiptWithDependencies(oldId: String) {
        val receipt = getReceiptById(oldId)
        if (receipt != null) {
            deleteReceiptById(oldId)
            receipt.id = receipt.firestoreId
            receipt.updatedAt = DateUtil.getCurrentUtcTime()
            insertReceipt(receipt)
            updateDependentProductByReceiptId(oldId, receipt.id, receipt.updatedAt)
        }
    }

    @Query("DELETE FROM Category WHERE id = :id")
    suspend fun deleteCategoryById(id: String)

    @Query("DELETE FROM Store WHERE id = :id")
    suspend fun deleteStoreById(id: String)

    @Query("DELETE FROM Receipt WHERE id = :id")
    suspend fun deleteReceiptById(id: String)

    @Query("UPDATE store SET defaultCategoryId = :newCategoryId, updatedAt = :timestamp WHERE defaultCategoryId = :oldCategoryId")
    suspend fun updateDependentStoreByCategoryId(
        oldCategoryId: String,
        newCategoryId: String,
        timestamp: String
    )

    @Query("UPDATE receipt SET storeId = :newStoreId, updatedAt = :timestamp WHERE storeId = :oldStoreId")
    suspend fun updateDependentReceiptByStoreId(
        oldStoreId: String,
        newStoreId: String,
        timestamp: String
    )

    @Query("UPDATE product SET receiptId = :newReceiptId, updatedAt = :timestamp WHERE receiptId = :oldReceiptId")
    suspend fun updateDependentProductByReceiptId(
        oldReceiptId: String,
        newReceiptId: String,
        timestamp: String
    )

}
