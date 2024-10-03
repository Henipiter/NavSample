package com.example.navsample.entities

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Category::class,
        Product::class,
        Receipt::class,
        Store::class,
        User::class
    ],
    version = 1
)
abstract class ReceiptDatabase : RoomDatabase() {

    abstract val receiptDao: ReceiptDao

    companion object {
        @Volatile
        private var INSTANCE: ReceiptDatabase? = null

        fun getInstance(context: Context): ReceiptDatabase {
            synchronized(this) {
                return INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext, ReceiptDatabase::class.java,
                    "receipt_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}