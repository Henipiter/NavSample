package com.example.navsample.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.navsample.entities.relations.CategoryWithProducts
import com.example.navsample.entities.relations.ReceiptWithProducts
import com.example.navsample.entities.relations.StoreWithReceipts

@Dao
interface ReceiptDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: Receipt): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStore(store: Store)

    @Transaction
    @Query("SELECT * FROM receipt WHERE id = :id")
    suspend fun getReceipt(id: Int): Receipt
    @Transaction
    @Query("SELECT id FROM receipt WHERE rowId = :rowId")
    suspend fun getReceiptId(rowId: Long): Int

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
    @Query("SELECT * FROM store WHERE nip = :nip")
    suspend fun getStoreWithReceipts(nip: String): List<StoreWithReceipts>

    @Transaction
    @Query("SELECT * FROM category")
    suspend fun getAllCategory(): List<Category>

    @Transaction
    @Query("SELECT * FROM category WHERE name = :name")
    suspend fun getCategoryWithProducts(name: String): List<CategoryWithProducts>

}
