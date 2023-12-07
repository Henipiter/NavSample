package com.example.navsample.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.navsample.entities.relations.PriceByCategory
import com.example.navsample.entities.relations.ProductWithCategory
import com.example.navsample.entities.relations.ReceiptWithProducts
import com.example.navsample.entities.relations.ReceiptWithStore

@Dao
interface ReceiptDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReceipt(receipt: Receipt): Long

    @Update
    suspend fun updateReceipt(receipt: Receipt)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: Category)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertStore(store: Store): Long

    @Update
    suspend fun updateStore(store: Store)

    @Transaction
    @Query("SELECT * FROM receipt WHERE id = :id")
    suspend fun getReceipt(id: Int): Receipt

    @Transaction
    @Query("SELECT id FROM receipt WHERE rowId = :rowId")
    suspend fun getReceiptId(rowId: Long): Int

    @Transaction
    @Query("SELECT id FROM store WHERE rowId = :rowId")
    suspend fun getStoreId(rowId: Long): Int

    @Transaction
    @Query("SELECT * FROM receipt WHERE id = :id")
    suspend fun getReceiptWithProducts(id: Int): List<ReceiptWithProducts>

    @Transaction
    @Query("SELECT * FROM store WHERE nip = :nip")
    suspend fun getStoreByNip(nip: String): Store

    @Transaction
    @Query("SELECT * FROM store WHERE id = :id")
    suspend fun getStoreById(id: Int): Store

    @Transaction
    @Query("SELECT * FROM store")
    suspend fun getAllStores(): List<Store>

    @Transaction
    @Query("SELECT r.id as id, storeId, nip, name, pln, ptu, date, time  FROM receipt r, store s WHERE s.id = r.storeId AND s.name LIKE '%' || :name || '%' ORDER BY date DESC")
    suspend fun getReceiptWithStore(name: String): List<ReceiptWithStore>

    @Transaction
    @Query("SELECT r.id as id, storeId, nip, name, pln, ptu, date, time  FROM receipt r, store s WHERE s.id = r.storeId AND s.name LIKE '%' || :name || '%' and r.date >= :dateFrom and r.date <= :dateTo ORDER BY date DESC")
    suspend fun getReceiptWithStore(
        name: String,
        dateFrom: String,
        dateTo: String,
    ): List<ReceiptWithStore>

    @Transaction
    @Query("SELECT * FROM category")
    suspend fun getAllCategories(): List<Category>

    @Transaction
    @Query("SELECT * FROM receipt")
    suspend fun getAllReceipts(): List<Receipt>

    @Transaction
    @Query("SELECT * FROM product WHERE receiptId=:receiptId")
    suspend fun getAllProducts(receiptId: Int): List<Product>

    @Transaction
    @Query("SELECT * FROM product WHERE id = :id")
//    @Query("SELECT * FROM product p, category c WHERE p.id = :id AND p.categoryId = c.id")
    suspend fun getCategoryWithProduct(id: Int): List<ProductWithCategory>

    //charts
    @Transaction
    @Query(
        "SELECT sum(p.finalPrice) AS price, c.name AS category, substr(r.date,0,8) AS date " +
                "FROM product p, receipt r, category c " +
                "WHERE p.receiptId = r.id AND p.categoryId = c.id " +
                "AND  date>=:dateFrom AND date<=:dateTo " +
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
                "GROUP BY categoryId " +
                "ORDER BY pln DESC"
    )
    suspend fun getPricesForCategoryComparison(
        dateFrom: String = "0",
        dateTo: String = "9",
    ): List<PriceByCategory>


}
