package com.example.navsample

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import com.example.navsample.DTO.Category
import com.example.navsample.DTO.Product

class DatabaseHelper(val context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "Products.db"
        const val PRODUCT_TABLE_NAME = "products"
        const val ID_COLUMN = "_id"
        const val NAME_COLUMN = "name"
        const val PRICE_COLUMN = "price"
        const val CATEGORY_COLUMN = "category"
        const val RECEIPT_ID_COLUMN = "receipt_id"
        const val CATEGORY_TABLE_NAME = "categories"
        const val STORE_TABLE_NAME = "stores"
        const val STORE_COLUMN = "store"

        const val ID_CURSOR_POSITION = 0
        const val PRODUCT_NAME_CURSOR_POSITION = 1
        const val PRODUCT_PRICE_CURSOR_POSITION = 2
        const val PRODUCT_CATEGORY_CURSOR_POSITION = 3
        const val PRODUCT_RECEIPT_CURSOR_POSITION = 4
        const val CATEGORY_CATEGORY_CURSOR_POSITION = 1
        const val STORE_STORE_CURSOR_POSITION = 1

    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createProductTableQuery = (
                "CREATE TABLE $PRODUCT_TABLE_NAME ( " +
                        "$ID_COLUMN  INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "$NAME_COLUMN TEXT, " +
                        "$PRICE_COLUMN REAL, " +
                        "$CATEGORY_COLUMN TEXT, " +
                        "$RECEIPT_ID_COLUMN  TEXT );")
        val createCategoryTableQuery = (
                "CREATE TABLE $CATEGORY_TABLE_NAME ( " +
                        "$ID_COLUMN  INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "$CATEGORY_COLUMN TEXT );")
        val createStoreTableQuery = (
                "CREATE TABLE $STORE_TABLE_NAME ( " +
                        "$ID_COLUMN  INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "$STORE_COLUMN TEXT );")
        db?.execSQL(createProductTableQuery)
        db?.execSQL(createCategoryTableQuery)
        db?.execSQL(createStoreTableQuery)

        //init categories
        val categories: Array<out String> =
            context?.resources?.getStringArray(R.array.countries_array) ?: arrayOf()
        for (index in categories.indices) {
            db?.execSQL(
                "insert into $CATEGORY_TABLE_NAME ($ID_COLUMN,$CATEGORY_COLUMN) values($index,'"+ categories[index]+"' )"
            )
            Log.i("query","insert")
        }

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $PRODUCT_TABLE_NAME")
        //onCreate(db)
    }

    fun deleteProduct(id: String): Boolean {
        val db = this.writableDatabase
        val result = db.delete(PRODUCT_TABLE_NAME, "$ID_COLUMN=$id", null) > 0
//        db.close()
        return result
    }

    fun deleteCategory(id: String): Boolean {
        val db = this.writableDatabase
        val result = db.delete(PRODUCT_TABLE_NAME, "$ID_COLUMN=$id", null) > 0
        return result
    }

    fun addCategory(category: Category) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(CATEGORY_COLUMN, category.category)
        db.insert(CATEGORY_TABLE_NAME, null, contentValues)
    }

    fun addProduct(product: Product) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(NAME_COLUMN, product.name)
        contentValues.put(PRICE_COLUMN, product.price)
        contentValues.put(CATEGORY_COLUMN, product.category)
        contentValues.put(RECEIPT_ID_COLUMN, product.receiptId)

        val result = db.insert(PRODUCT_TABLE_NAME, null, contentValues)
        if (result == (-1).toLong()) {
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
        }
//        db.close()
    }

    fun readAllCategoryData(): ArrayList<Category> {
        val query = "Select * from $CATEGORY_TABLE_NAME ORDER BY $CATEGORY_COLUMN;"
        Log.i("query", query)
        val db = this.readableDatabase
        var cursor: Cursor? = null
        if (db != null) {
            cursor = db.rawQuery(query, null)
        }
        return cursorToCategories(cursor)
    }

    fun readAllProductData(): ArrayList<Product> {
//        val query = "Select * from $TABLE_NAME where $RECEIPT_ID_COLUMN='$data' ORDER BY $CATEGORY_COLUMN;"
        val query = "Select * from $PRODUCT_TABLE_NAME ORDER BY $CATEGORY_COLUMN;"
        Log.i("query", query)
        val db = this.readableDatabase
        var cursor: Cursor? = null
        if (db != null) {
            cursor = db.rawQuery(query, null)
        }
        val result = cursorToProducts(cursor)
        return result
    }

    fun readOneProductData(id: String): Product {
        val query = "Select * from $PRODUCT_TABLE_NAME where $ID_COLUMN='$id';"
        Log.i("query", query)
        val db = this.readableDatabase
        var cursor: Cursor? = null
        if (db != null) {
            cursor = db.rawQuery(query, null)
        }
        val result = cursorToProducts(cursor, id)
        return result
    }

    private fun cursorToCategories(cursor: Cursor?): ArrayList<Category> {
        val categories = ArrayList<Category>()
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val category = Category(
                    cursor.getString(ID_CURSOR_POSITION),
                    cursor.getString(CATEGORY_CATEGORY_CURSOR_POSITION)
                )
                categories.add(category)
            }
        }
        return categories
    }

    private fun cursorToProducts(cursor: Cursor?): ArrayList<Product> {
        val products = ArrayList<Product>()
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val product = Product(
                    cursor.getString(ID_CURSOR_POSITION),
                    cursor.getString(PRODUCT_NAME_CURSOR_POSITION),
                    cursor.getFloat(PRODUCT_PRICE_CURSOR_POSITION),
                    cursor.getString(PRODUCT_CATEGORY_CURSOR_POSITION),
                    cursor.getString(PRODUCT_RECEIPT_CURSOR_POSITION)
                )
                products.add(product)
            }
        }
        return products
    }

    private fun cursorToProducts(cursor: Cursor?, id: String): Product {
        var product = Product()
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.getString(0) == id) {
                    product = Product(
                        cursor.getString(ID_CURSOR_POSITION),
                        cursor.getString(PRODUCT_NAME_CURSOR_POSITION),
                        cursor.getFloat(PRODUCT_PRICE_CURSOR_POSITION),
                        cursor.getString(PRODUCT_CATEGORY_CURSOR_POSITION),
                        cursor.getString(PRODUCT_RECEIPT_CURSOR_POSITION)
                    )
                    break
                }
            }
        }
        return product
    }
}
