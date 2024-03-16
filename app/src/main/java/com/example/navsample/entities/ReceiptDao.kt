package com.example.navsample.entities

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.navsample.entities.relations.AllData
import com.example.navsample.entities.relations.PriceByCategory
import com.example.navsample.entities.relations.ProductRichData
import com.example.navsample.entities.relations.ReceiptWithStore
import com.example.navsample.entities.relations.TableCounts

@Dao
interface ReceiptDao {

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

    @Delete
    suspend fun deleteStore(store: Store)

    @Delete
    suspend fun deleteReceipt(receipt: Receipt)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Transaction
    @Query("delete from product where id = :id")
    suspend fun deleteProductById(id: Int)

    @Transaction
    @Query("delete from receipt where id = :id")
    suspend fun deleteReceiptById(id: Int)

    @Transaction
    @Query("delete from product where id in (select p.id from product p, receipt r  where p.receiptId = r.id   and r.id = :id)")
    suspend fun deleteProductsOfReceipt(id: Int)

    @Transaction
    @Query("delete from receipt where id in  (select r.id from receipt r, store s where s.id = r.storeId and s.id = :id)")
    suspend fun deleteReceiptsOfStore(id: Int)

    @Transaction
    @Query("delete from product where id in (select p.id from product p, receipt r, store s where p.receiptId = r.id and s.id = r.storeId and s.id = :id)")
    suspend fun deleteProductsOfStore(id: Int)

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
    @Query("SELECT id FROM category WHERE rowId = :rowId")
    suspend fun getCategoryId(rowId: Long): Int

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
    @Query("SELECT r.id as id, storeId, nip, s.name, pln, ptu, date, time, count(p.id)  as productCount FROM receipt r, store s, product p WHERE s.id = r.storeId AND r.id = p.receiptId AND s.name LIKE '%' || :name || '%' GROUP BY r.id ORDER BY date DESC")
    suspend fun getReceiptWithStore(name: String): List<ReceiptWithStore>

    @Transaction
    @Query("SELECT r.id as id, storeId, nip, s.name, pln, ptu, date, time, count(p.id)  as productCount FROM receipt r, store s, product p WHERE s.id = r.storeId AND r.id = p.receiptId AND s.name LIKE '%' || :name || '%' and r.date >= :dateFrom and r.date <= :dateTo GROUP BY r.id ORDER BY date DESC")
    suspend fun getReceiptWithStore(
        name: String,
        dateFrom: String,
        dateTo: String,
    ): List<ReceiptWithStore>

    @Transaction
    @Query("SELECT * FROM category order by name")
    suspend fun getAllCategories(): List<Category>

    @Transaction
    @Query("SELECT * FROM product WHERE receiptId=:receiptId")
    suspend fun getAllProducts(receiptId: Int): List<Product>

    @Transaction
    @Query(
        "select s.name as storeName, r.date, c.name as categoryName, c.color as categoryColor, p.* from product p, receipt r, store s, category c " +
                "where p.receiptId = r.id and s.id =r.storeId " +
                "and s.name like '%'||:storeName||'%' " +
                "and c.name like '%'||:categoryName||'%' " +
                "and r.date >= :dateFrom and r.date <= :dateTo " +
                "and p.subtotalPrice >= :lowerPrice and p.subtotalPrice <=:higherPrice"
    )
    suspend fun getAllProducts(
        storeName: String,
        categoryName: String,
        dateFrom: String,
        dateTo: String,
        lowerPrice: Float,
        higherPrice: Float,
    ): List<ProductRichData>

    @Transaction
    @Query(
        "select s.name as storeName, r.date, c.name as categoryName, c.color as categoryColor, p.* from product p, receipt r, store s, category c " +
                "where p.receiptId = r.id and s.id =r.storeId and p.categoryId = c.id " +
                "and s.name like '%'||:storeName||'%' " +
                "and c.name like '%'||:categoryName||'%' " +
                "and r.date >= :dateFrom and r.date <= :dateTo " +
                "and p.subtotalPrice >= :lowerPrice"
    )
    suspend fun getAllProducts(
        storeName: String,
        categoryName: String,
        dateFrom: String,
        dateTo: String,
        lowerPrice: Float,
    ): List<ProductRichData>

    @Transaction
    @Query(
        "select 'store' as 'tableName', count(*) as 'count' from store " +
                "union all select 'product' as 'tableName', count(*)  as 'count' from product " +
                "union all select 'category' as 'tableName', count(*) as 'count'  from category " +
                "union all select 'receipt' as 'tableName', count(*) as 'count'  from receipt"
    )
    suspend fun getTableCounts(): List<TableCounts>

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

    @Transaction
    @Query(
        "select s.name as storeName, s.nip as storeNip, " +
                "r.pln as receiptPln, r.ptu as receiptPtu, " +
                "r.date as receiptDate, r.time as receiptTime, " +
                "p.name as productName, p.quantity as productQuantity, " +
                "p.unitPrice as productUnitPrice, p.subtotalPrice as productSubtotalPrice, " +
                "p.discount as productDiscount, p.finalPrice as productFinalPrice, " +
                "p.ptuType as productPtuType, p.raw as productRaw, " +
                "c.name as categoryName, c.color as categoryColor " +
                "from product p, receipt r, store s, category c " +
                "where p.receiptId = r.id and s.id =r.storeId and p.categoryId = c.id "
    )
    suspend fun getAllData(): List<AllData>


}
