package com.example.navsample.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.navsample.entities.relations.ReceiptWithProducts

@Dao
interface ReceiptDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: Receipt)

    @Transaction
    @Query("SELECT * FROM receipt WHERE id = :id")
    suspend fun getReceipt(id:Int): List<Receipt>
    @Transaction
    @Query("SELECT * FROM receipt WHERE id = :id")
    suspend fun getReceiptWkithProducts(id:Int): List<ReceiptWithProducts>

}
