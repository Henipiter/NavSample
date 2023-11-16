package com.example.navsample.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.navsample.entities.relations.CategoryWithProducts
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
    suspend fun getStore(nip: String): Store

    @Transaction
    @Query("SELECT * FROM store")
    suspend fun getAllStores(): List<Store>

    @Transaction
    @Query("SELECT * FROM receipt r, store s WHERE s.nip = r.nip AND s.name LIKE '%' || :name || '%'")
    suspend fun getReceiptWithStore(name: String): List<ReceiptWithStore>

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
    @Query("SELECT * FROM category WHERE name = :name")
    suspend fun getCategoryWithProducts(name: String): List<CategoryWithProducts>


}
