package com.example.navsample.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.navsample.dto.DateUtil
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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReceipt(receipt: Receipt): Long

    @Update
    suspend fun updateReceipt(receipt: Receipt)

    @Update
    suspend fun updateProduct(product: Product)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Transaction
    @Query("UPDATE category SET deletedAt = :deletedAt WHERE id = :id")
    suspend fun deleteCategoryById(id: String, deletedAt: String)

    @Transaction
    @Query("SELECT * FROM category WHERE deletedAt = :deletedAt AND id = :id")
    suspend fun selectDeletedCategoryById(id: String, deletedAt: String): Category

    @Transaction
    suspend fun deleteAndSelectCategoryById(id: String, deletedAt: String): Category {
        deleteCategoryById(id, deletedAt)
        return selectDeletedCategoryById(id, deletedAt)
    }

    @Transaction
    @Query("UPDATE store SET deletedAt = :deletedAt WHERE id = :id")
    suspend fun deleteStoreById(id: String, deletedAt: String)

    @Transaction
    @Query("SELECT * FROM store WHERE deletedAt = :deletedAt AND id = :id")
    suspend fun selectDeletedStoreById(id: String, deletedAt: String): Store

    @Transaction
    suspend fun deleteAndSelectStore(id: String, deletedAt: String): Store {
        deleteStoreById(id, deletedAt)
        return selectDeletedStoreById(id, deletedAt)
    }

    @Transaction
    @Query("UPDATE product SET deletedAt = :deletedAt WHERE id = :id")
    suspend fun deleteProductById(id: String, deletedAt: String)

    @Transaction
    @Query("SELECT * FROM product WHERE deletedAt = :deletedAt AND id = :id")
    suspend fun selectDeletedProductById(id: String, deletedAt: String): Product

    @Transaction
    suspend fun deleteAndSelectProductById(id: String, deletedAt: String): Product {
        deleteProductById(id, deletedAt)
        return selectDeletedProductById(id, deletedAt)
    }

    @Transaction
    @Query("UPDATE receipt SET deletedAt = :deletedAt WHERE id = :id")
    suspend fun deleteReceiptById(id: String, deletedAt: String)

    @Transaction
    @Query("SELECT * FROM receipt WHERE deletedAt = :deletedAt AND id = :id")
    suspend fun selectDeletedReceiptById(id: String, deletedAt: String): Receipt
    suspend fun deleteAndSelectReceiptById(id: String, deletedAt: String): Receipt {
        deleteReceiptById(id, deletedAt)
        return selectDeletedReceiptById(id, deletedAt)
    }

    @Transaction
    @Query(
        "UPDATE product SET deletedAt = :deletedAt WHERE id IN (" +
                "SELECT p.id FROM product p, receipt r " +
                "WHERE p.receiptId = r.id AND r.id = :id)"
    )
    suspend fun deleteProductsOfReceipt(id: String, deletedAt: String)

    @Transaction
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

    @Transaction
    @Query(
        "UPDATE receipt SET deletedAt = :deletedAt WHERE id IN (" +
                "SELECT r.id FROM receipt r, store s " +
                "WHERE s.id = r.storeId AND s.id = :id" +
                ")"
    )
    suspend fun deleteReceiptsOfStore(id: String, deletedAt: String)

    @Transaction
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

    @Transaction
    @Query(
        "UPDATE product SET deletedAt = :deletedAt WHERE id IN (" +
                "SELECT p.id FROM product p, receipt r, store s " +
                "WHERE p.receiptId = r.id AND s.id = r.storeId AND s.id = :id " +
                ")"
    )
    suspend fun deleteProductsOfStore(id: String, deletedAt: String)

    @Transaction
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

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertStore(store: Store): Long

    @Update
    suspend fun updateStore(store: Store)

    @Transaction
    @Query("SELECT * FROM receipt WHERE id = :id AND deletedAt == ''")
    suspend fun getReceipt(id: String): Receipt

    @Transaction
    @Query("SELECT * FROM category WHERE id = :id AND deletedAt == ''")
    suspend fun getCategoryById(id: String): Category

    @Transaction
    @Query("SELECT uuid FROM user WHERE id = 0")
    suspend fun getUserUuid(): String?

    @Transaction
    @Query("SELECT * FROM store WHERE nip = :nip AND deletedAt == ''")
    suspend fun getStoreByNip(nip: String): Store

    @Transaction
    @Query("SELECT * FROM store WHERE id = :id AND deletedAt == ''")
    suspend fun getStoreById(id: String): Store

    @Transaction
    @Query("SELECT * FROM product WHERE id = :id AND deletedAt == ''")
    suspend fun getProductById(id: String): Product

    @Transaction
    @Query("SELECT * FROM receipt WHERE id = :id AND deletedAt == ''")
    suspend fun getReceiptById(id: String): Receipt

    @Transaction
    @Query("SELECT * FROM store WHERE deletedAt == '' ORDER BY name")
    suspend fun getAllStores(): List<Store>

    @Transaction
    @Query("SELECT * FROM store WHERE deletedAt == '' AND defaultCategoryId = :categoryId")
    suspend fun getAllStoresWithCategoryId(categoryId: String): List<Store>

    @Transaction
    @Query("SELECT * FROM receipt WHERE deletedAt == '' AND storeId = :storeId")
    suspend fun getAllReceiptWithStoreId(storeId: String): List<Receipt>

    @Transaction
    @Query("SELECT  s.id,s.updatedAt,s.isSync,  c.id as categoryId FROM store s INNER JOIN category c ON s.defaultCategoryId = c.id WHERE s.firestoreId == :firestoreId ")
    suspend fun getStoreForFirestore(firestoreId: String): StoreFirebase?

    @Transaction
    suspend fun updateCategoryIdInStore(oldCategoryId: String, newCategoryId: String) {
        val stores = getAllStoresWithCategoryId(oldCategoryId)
        stores.forEach {
            it.defaultCategoryId = newCategoryId
            it.updatedAt = DateUtil.getCurrentUtcTime()
            updateStore(it)
            //TODO Firestore update if firestoreId = id && firestire!= ""
        }
    }

    @Transaction
    @RawQuery
    suspend fun getAllStoresOrdered(query: SupportSQLiteQuery): List<Store>

    @Transaction
    @RawQuery
    suspend fun getReceiptWithStoreOrdered(query: SupportSQLiteQuery): List<ReceiptWithStore>

    @Transaction
    @Query("SELECT * FROM category WHERE deletedAt == '' ORDER BY name")
    suspend fun getAllCategories(): List<Category>

    @Transaction
    @Query("SELECT * FROM category WHERE name LIKE '%'||:name||'%' AND deletedAt == '' ORDER BY name")
    suspend fun getAllCategories(name: String): List<Category>

    @Transaction
    @Query("SELECT * FROM product WHERE receiptId=:receiptId AND deletedAt == ''")
    suspend fun getAllProducts(receiptId: String): List<Product>

    @Transaction
    @RawQuery
    suspend fun getAllProductsOrderedWithHigherPrice(query: SupportSQLiteQuery): List<ProductRichData>

    @Transaction
    @Query(
        "SELECT 'store' AS 'tableName', count(*) AS 'count' FROM store WHERE deletedAt == '' " +
                "UNION ALL SELECT 'product' AS 'tableName', count(*)  AS 'count' FROM product WHERE deletedAt == '' " +
                "UNION ALL SELECT 'category' AS 'tableName', count(*) AS 'count' FROM category WHERE deletedAt == '' " +
                "UNION ALL SELECT 'receipt' AS 'tableName', count(*) AS 'count' FROM receipt WHERE deletedAt == '' "
    )
    suspend fun getTableCounts(): List<TableCounts>

    //charts
    @Transaction
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

    @Transaction
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

    @Transaction
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
    @Query("delete from store where id = :id")
    suspend fun deleteStoreById(id: String)

    @Transaction
    @Query("delete from product where id = :id")
    suspend fun deleteProductById(id: String)

    @Transaction
    @Query("delete from receipt where id = :id")
    suspend fun deleteReceiptById(id: String)

    @Transaction
    @Query("delete from category where id = :id")
    suspend fun deleteCategoryById(id: String)

    @Transaction
    @Query("select isSync from category where id = :categoryId")
    suspend fun isCategorySynced(categoryId: String): Boolean

}
