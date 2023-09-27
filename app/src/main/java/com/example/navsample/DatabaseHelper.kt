package com.example.navsample

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast

class DatabaseHelper(val context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "Products.db"
        const val TABLE_NAME = "products"
        const val ID_COLUMN = "_id"
        const val NAME_COLUMN = "name"
        const val PRICE_COLUMN = "price"
        const val CATEGORY_COLUMN = "category"
        const val RECEIPT_ID_COLUMN = "receipt_id"

        const val ID_CURSOR_POSITION = 0
        const val NAME_CURSOR_POSITION = 1
        const val PRICE_CURSOR_POSITION = 2
        const val CATEGORY_CURSOR_POSITION = 3
        const val RECEIPT_CURSOR_POSITION = 4
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = (
                "CREATE TABLE $TABLE_NAME ( " +
                        "$ID_COLUMN  INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "$NAME_COLUMN TEXT, " +
                        "$PRICE_COLUMN REAL, " +
                        "$CATEGORY_COLUMN TEXT, " +
                        "$RECEIPT_ID_COLUMN  TEXT );")
        db?.execSQL(createTableQuery)
//        db?.close()
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        //onCreate(db)
    }

    fun deleteEvent(id: String): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_NAME, "$ID_COLUMN=$id", null) > 0
//        db.close()
        return result
    }

    fun addProduct(product: Product) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(NAME_COLUMN, product.name)
        contentValues.put(PRICE_COLUMN, product.price)
        contentValues.put(CATEGORY_COLUMN, product.category)
        contentValues.put(RECEIPT_ID_COLUMN, product.receiptId)

        val result = db.insert(TABLE_NAME, null, contentValues)
        if (result == (-1).toLong()) {
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
        }
//        db.close()
    }



    fun readAllData(): ArrayList<Product> {
//        val query = "Select * from $TABLE_NAME where $RECEIPT_ID_COLUMN='$data' ORDER BY $CATEGORY_COLUMN;"
        val query = "Select * from $TABLE_NAME ORDER BY $CATEGORY_COLUMN;"
        Log.e("query", query)
        val db = this.readableDatabase
        var cursor: Cursor? = null
        if (db != null) {
            cursor = db.rawQuery(query, null)
        }
        val result = cursorToNotes(cursor)
//        db.close()
        return result
    }

    fun readOneData(id: String): Product {
        val query = "Select * from $TABLE_NAME where $ID_COLUMN='$id';"
        Log.e("query", query)
        val db = this.readableDatabase
        var cursor: Cursor? = null
        if (db != null) {
            cursor = db.rawQuery(query, null)
        }
        val result = cursorToNote(cursor, id)
//        db.close()
        return result
    }

    private fun cursorToNotes(cursor: Cursor?): ArrayList<Product> {
        val products = ArrayList<Product>()
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val product = Product(
                    cursor.getString(ID_CURSOR_POSITION),
                    cursor.getString(NAME_CURSOR_POSITION),
                    cursor.getFloat(PRICE_CURSOR_POSITION),
                    cursor.getString(CATEGORY_CURSOR_POSITION),
                    cursor.getString(RECEIPT_CURSOR_POSITION)
                )
                products.add(product)
            }
        }
        return products
    }

    private fun cursorToNote(cursor: Cursor?, id: String): Product {
        var product = Product()
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.getString(0) == id) {
                    product = Product(
                        cursor.getString(ID_CURSOR_POSITION),
                        cursor.getString(NAME_CURSOR_POSITION),
                        cursor.getFloat(PRICE_CURSOR_POSITION),
                        cursor.getString(CATEGORY_CURSOR_POSITION),
                        cursor.getString(RECEIPT_CURSOR_POSITION)
                    )
                    break
                }
            }
        }
        return product
    }
}
