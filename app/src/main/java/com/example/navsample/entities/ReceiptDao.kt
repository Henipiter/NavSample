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
import com.example.navsample.entities.database.Category
import com.example.navsample.entities.database.Product
import com.example.navsample.entities.database.ProductTagCrossRef
import com.example.navsample.entities.database.Receipt
import com.example.navsample.entities.database.Store
import com.example.navsample.entities.database.Tag
import com.example.navsample.entities.firestore.CategoryFirebase
import com.example.navsample.entities.firestore.ProductFirebase
import com.example.navsample.entities.firestore.ProductTagCrossRefFirebase
import com.example.navsample.entities.firestore.ReceiptFirebase
import com.example.navsample.entities.firestore.StoreFirebase
import com.example.navsample.entities.firestore.TagFirebase
import com.example.navsample.entities.relations.AllData
import com.example.navsample.entities.relations.PriceByCategory
import com.example.navsample.entities.relations.ProductRichData
import com.example.navsample.entities.relations.ProductWithTag
import com.example.navsample.entities.relations.ReceiptWithStore
import com.example.navsample.entities.relations.TableCounts

@Dao
interface ReceiptDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: Tag)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStore(store: Store)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductTag(productTagCrossRef: ProductTagCrossRef)

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
                "updatedAt = :updatedAt, " +
                "toUpdate = :toUpdate " +
                "WHERE id = :id"
    )
    suspend fun updateCategoryFields(
        id: String,
        name: String,
        color: String,
        updatedAt: String,
        toUpdate: Boolean = true
    )

    @Query(
        "UPDATE store " +
                "SET nip = :nip, " +
                "name = :name, " +
                "defaultCategoryId= :defaultCategoryId, " +
                "updatedAt = :updatedAt, " +
                "toUpdate = :toUpdate " +
                "WHERE id = :id"
    )
    suspend fun updateStoreFields(
        id: String,
        name: String,
        nip: String,
        defaultCategoryId: String,
        updatedAt: String,
        toUpdate: Boolean = true
    )

    @Query(
        "UPDATE tag " +
                "SET name = :name, " +
                "updatedAt = :updatedAt, " +
                "toUpdate = :toUpdate " +
                "WHERE id = :id"
    )
    suspend fun updateTagFields(
        id: String,
        name: String,
        updatedAt: String,
        toUpdate: Boolean = true
    )

    @Query(
        "UPDATE receipt " +
                "SET date = :date, " +
                "time = :time, " +
                "pln = :pln, " +
                "ptu = :ptu, " +
                "storeId = :storeId, " +
                "updatedAt = :updatedAt, " +
                "toUpdate = :toUpdate " +
                "WHERE id = :id"
    )
    suspend fun updateReceiptFields(
        id: String,
        date: String,
        time: String,
        pln: Int,
        ptu: Int,
        storeId: String,
        updatedAt: String,
        toUpdate: Boolean = true
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
                "updatedAt = :updatedAt, " +
                "toUpdate = :toUpdate " +
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
        updatedAt: String,
        toUpdate: Boolean = true
    )

    @Query(
        "UPDATE ProductTagCrossRef " +
                "SET productId = :productId, " +
                "tagId = :tagId, " +
                "updatedAt = :updatedAt, " +
                "toUpdate = :toUpdate " +
                "WHERE id = :id"
    )
    suspend fun updateProductTagFields(
        id: String,
        productId: String,
        tagId: String,
        updatedAt: String,
        toUpdate: Boolean = true
    )


    @Transaction
    suspend fun saveCategoryFromFirestore(category: Category): Boolean {
        if (category.id == "") {
            return false
        }
        val localCategory = getCategoryById(category.id)
        if (localCategory == null) {
            insertCategory(category)
            return true
        } else {
            if (localCategory.updatedAt < category.updatedAt) {
                updateCategoryFields(
                    category.id,
                    category.name,
                    category.color,
                    category.updatedAt,
                    false
                )
                return true
            }
        }
        return false
    }

    @Transaction
    suspend fun saveStoreFromFirestore(store: Store): Boolean {
        if (store.id == "") {
            return false
        }
        val localStore = getStoreById(store.id)
        if (localStore == null) {
            insertStore(store)
            return true
        } else {
            if (localStore.updatedAt < store.updatedAt) {
                updateStoreFields(
                    store.id,
                    store.name,
                    store.nip,
                    store.defaultCategoryId,
                    store.updatedAt,
                    false
                )
                return true
            }
        }
        return false
    }

    @Transaction
    suspend fun saveReceiptFromFirestore(receipt: Receipt): Boolean {
        if (receipt.id == "") {
            return false
        }
        val localReceipt = getReceiptById(receipt.id)
        if (localReceipt == null) {
            insertReceipt(receipt)
            return true
        } else {
            if (localReceipt.updatedAt < receipt.updatedAt) {
                updateReceiptFields(
                    receipt.id,
                    receipt.date,
                    receipt.time,
                    receipt.pln,
                    receipt.ptu,
                    receipt.storeId,
                    receipt.updatedAt,
                    false
                )
                return true
            }
        }
        return false
    }

    @Transaction
    suspend fun saveProductFromFirestore(product: Product): Boolean {
        if (product.id == "") {
            return false
        }
        val localProduct = getProductById(product.id)
        if (localProduct == null) {
            insertProduct(product)
            return true
        } else {
            if (localProduct.updatedAt < product.updatedAt) {
                updateProductFields(
                    product.id,
                    product.name,
                    product.categoryId,
                    product.quantity,
                    product.unitPrice,
                    product.subtotalPrice,
                    product.discount,
                    product.finalPrice,
                    product.raw,
                    product.ptuType,
                    product.validPrice,
                    product.updatedAt,
                    false
                )
                return true
            }
        }
        return false
    }

    @Transaction
    suspend fun saveTagFromFirestore(tag: Tag): Boolean {
        if (tag.id == "") {
            return false
        }
        val localProduct = getProductById(tag.id)
        if (localProduct == null) {
            insertTag(tag)
            return true
        } else {
            if (localProduct.updatedAt < tag.updatedAt) {
                updateTagFields(
                    tag.id,
                    tag.name,
                    tag.updatedAt,
                    false
                )
                return true
            }
        }
        return false
    }

    @Transaction
    suspend fun saveProductTagFromFirestore(productTag: ProductTagCrossRef): Boolean {
        if (productTag.id == "") {
            return false
        }
        val localProduct = getProductTagById(productTag.id)
        if (localProduct == null) {
            insertProductTag(productTag)
            return true
        } else {
            if (localProduct.updatedAt < productTag.updatedAt) {
                updateProductTagFields(
                    productTag.id,
                    productTag.productId,
                    productTag.tagId,
                    productTag.updatedAt,
                    false
                )
                return true
            }
        }
        return false
    }

    @Query("UPDATE category SET firestoreId = :firestoreId WHERE id = :id")
    suspend fun updateCategoryFirestoreId(id: String, firestoreId: String)

    @Query("UPDATE store SET firestoreId = :firestoreId WHERE id = :id")
    suspend fun updateStoreFirestoreId(id: String, firestoreId: String)

    @Query("UPDATE tag SET firestoreId = :firestoreId WHERE id = :id")
    suspend fun updateTagFirestoreId(id: String, firestoreId: String)

    @Query("UPDATE receipt SET firestoreId = :firestoreId WHERE id = :id")
    suspend fun updateReceiptFirestoreId(id: String, firestoreId: String)

    @Query("UPDATE product SET firestoreId = :firestoreId WHERE id = :id")
    suspend fun updateProductFirestoreId(id: String, firestoreId: String)

    @Query("UPDATE ProductTagCrossRef SET firestoreId = :firestoreId WHERE id = :id")
    suspend fun updateProductTagFirestoreId(id: String, firestoreId: String)

    @Query("UPDATE category SET deletedAt = :deletedAt, updatedAt = :deletedAt, toDelete = 1 WHERE id = :id")
    suspend fun deleteCategoryById(id: String, deletedAt: String)

    @Query("SELECT * FROM category WHERE toDelete = 1 AND id = :id")
    suspend fun selectDeletedCategoryById(id: String): Category

    @Transaction
    suspend fun deleteAndSelectCategoryById(id: String, deletedAt: String): Category {
        deleteCategoryById(id, deletedAt)
        return selectDeletedCategoryById(id)
    }

    @Query("UPDATE store SET deletedAt = :deletedAt, updatedAt = :deletedAt, toDelete = 1 WHERE id = :id")
    suspend fun deleteStoreById(id: String, deletedAt: String)

    @Query("SELECT * FROM store WHERE toDelete = 1 AND id = :id")
    suspend fun selectDeletedStoreById(id: String): Store

    suspend fun deleteAndSelectStore(id: String, deletedAt: String): Store {
        deleteStoreById(id, deletedAt)
        return selectDeletedStoreById(id)
    }

    @Query("UPDATE product SET deletedAt = :deletedAt, updatedAt = :deletedAt, toDelete = 1 WHERE id = :id")
    suspend fun deleteProductById(id: String, deletedAt: String)

    @Query(
        "UPDATE productTagCrossRef " +
                "SET deletedAt = :deletedAt, updatedAt = :deletedAt, toDelete = 1 " +
                "WHERE productId = :productId AND tagId = :tagId"
    )
    suspend fun deleteProductTag(productId: String, tagId: String, deletedAt: String)

    @Query("SELECT * FROM product WHERE toDelete = 1 AND id = :id")
    suspend fun selectDeletedProductById(id: String): Product

    @Query("SELECT * FROM ProductTagCrossRef WHERE toDelete = 1 AND productId = :productId AND tagId = :tagId")
    suspend fun selectDeletedProductTag(productId: String, tagId: String): ProductTagCrossRef

    @Transaction
    suspend fun deleteAndSelectProductById(id: String, deletedAt: String): Product {
        deleteProductById(id, deletedAt)
        return selectDeletedProductById(id)
    }

    @Transaction
    suspend fun deleteAndSelectProductTag(
        productId: String,
        tagId: String,
        deletedAt: String
    ): ProductTagCrossRef {
        deleteProductTag(productId, tagId, deletedAt)
        return selectDeletedProductTag(productId, tagId)
    }

    @Query("UPDATE receipt SET deletedAt = :deletedAt, updatedAt = :deletedAt, toDelete = 1 WHERE id = :id")
    suspend fun deleteReceiptById(id: String, deletedAt: String)

    @Query("UPDATE tag SET deletedAt = :deletedAt, updatedAt = :deletedAt, toDelete = 1 WHERE id = :id")
    suspend fun deleteTagById(id: String, deletedAt: String)

    @Query("SELECT * FROM receipt WHERE toDelete = 1 AND id = :id")
    suspend fun selectDeletedReceiptById(id: String): Receipt

    @Query("SELECT * FROM tag WHERE toDelete = 1 AND id = :id")
    suspend fun selectDeletedTagById(id: String): Tag

    @Transaction
    suspend fun deleteAndSelectReceiptById(id: String, deletedAt: String): Receipt {
        deleteReceiptById(id, deletedAt)
        return selectDeletedReceiptById(id)
    }

    @Transaction
    suspend fun deleteAndSelectTagById(id: String, deletedAt: String): Tag {
        deleteTagById(id, deletedAt)
        return selectDeletedTagById(id)
    }

    @Query(
        "UPDATE product SET deletedAt = :deletedAt, updatedAt = :deletedAt, toDelete = 1 WHERE id IN (" +
                "SELECT p.id FROM product p, receipt r " +
                "WHERE p.receiptId = r.id AND r.id = :id)"
    )
    suspend fun deleteProductsOfReceipt(id: String, deletedAt: String)

    @Query(
        "UPDATE ProductTagCrossRef SET deletedAt = :deletedAt, updatedAt = :deletedAt, toDelete = 1 WHERE id IN (" +
                "SELECT pt.id FROM ProductTagCrossRef pt, tag t " +
                "WHERE pt.tagId = t.id AND t.id = :id)"
    )
    suspend fun deleteProductTagsOfTag(id: String, deletedAt: String)

    @Query(
        "UPDATE ProductTagCrossRef SET deletedAt = :deletedAt, updatedAt = :deletedAt, toDelete = 1 WHERE id IN (" +
                "SELECT pt.id FROM ProductTagCrossRef pt, product p " +
                "WHERE pt.productId = p.id AND p.id = :id)"
    )
    suspend fun deleteProductTagsOfProduct(id: String, deletedAt: String)

    @Query(
        "SELECT * FROM product WHERE deletedAt = :deletedAt AND id IN (" +
                "SELECT p.id FROM product p, receipt r " +
                "WHERE p.receiptId = r.id AND r.id = :id)"
    )
    suspend fun selectDeletedProductsOfReceipt(id: String, deletedAt: String): List<Product>

    @Query(
        "SELECT * FROM ProductTagCrossRef WHERE deletedAt = :deletedAt AND id IN (" +
                "SELECT pt.id FROM ProductTagCrossRef pt, tag t " +
                "WHERE pt.tagId = t.id AND t.id = :id)"
    )
    suspend fun selectDeletedProductTagsOfTag(
        id: String,
        deletedAt: String
    ): List<ProductTagCrossRef>

    @Query(
        "SELECT * FROM ProductTagCrossRef WHERE deletedAt = :deletedAt AND id IN (" +
                "SELECT pt.id FROM ProductTagCrossRef pt, product p " +
                "WHERE pt.productId = p.id AND p.id = :id)"
    )
    suspend fun selectDeletedProductTagsOfProduct(
        id: String,
        deletedAt: String
    ): List<ProductTagCrossRef>

    @Transaction
    suspend fun deleteAndSelectProductsOfReceipt(id: String, deletedAt: String): List<Product> {
        deleteProductsOfReceipt(id, deletedAt)
        return selectDeletedProductsOfReceipt(id, deletedAt)
    }

    @Transaction
    suspend fun deleteAndSelectProductTagsOfTag(
        id: String,
        deletedAt: String
    ): List<ProductTagCrossRef> {
        deleteProductTagsOfTag(id, deletedAt)
        return selectDeletedProductTagsOfTag(id, deletedAt)
    }

    @Transaction
    suspend fun deleteAndSelectProductTagsOfProduct(
        id: String,
        deletedAt: String
    ): List<ProductTagCrossRef> {
        deleteProductTagsOfProduct(id, deletedAt)
        return selectDeletedProductTagsOfProduct(id, deletedAt)
    }

    @Query(
        "UPDATE receipt SET deletedAt = :deletedAt, updatedAt = :deletedAt, toDelete = 1 WHERE id IN (" +
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
        "UPDATE product SET deletedAt = :deletedAt, updatedAt = :deletedAt, toDelete = 1 WHERE id IN (" +
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

    @Query("SELECT * FROM store WHERE nip = :nip AND deletedAt == ''")
    suspend fun getStoreByNip(nip: String): Store

    @Query("SELECT * FROM tag WHERE id = :id AND deletedAt == ''")
    suspend fun getTagById(id: String): Tag?

    @Query("SELECT * FROM store WHERE id = :id AND deletedAt == ''")
    suspend fun getStoreById(id: String): Store?

    @Query("SELECT * FROM product WHERE id = :id AND deletedAt == ''")
    suspend fun getProductById(id: String): Product?

    @Query("SELECT * FROM ProductTagCrossRef WHERE id = :id AND deletedAt == ''")
    suspend fun getProductTagById(id: String): ProductTagCrossRef?

    @Query("SELECT * FROM receipt WHERE id = :id AND deletedAt == ''")
    suspend fun getReceiptById(id: String): Receipt?

    @Query("SELECT * FROM store WHERE deletedAt == '' ORDER BY name")
    suspend fun getAllStores(): List<Store>

    @Query("UPDATE category SET isSync = 1 WHERE id = :id")
    suspend fun syncCategory(id: String)

    @Query("UPDATE store SET isSync = 1 WHERE id = :id")
    suspend fun syncStore(id: String)

    @Query("UPDATE receipt SET isSync = 1 WHERE id = :id")
    suspend fun syncReceipt(id: String)

    @Query("UPDATE product SET isSync = 1 WHERE id = :id")
    suspend fun syncProduct(id: String)

    @Query("UPDATE tag SET isSync = 1 WHERE id = :id")
    suspend fun syncTag(id: String)

    @Query("UPDATE ProductTagCrossRef SET isSync = 1 WHERE id = :id")
    suspend fun syncProductTag(id: String)

    @Query("UPDATE category SET toUpdate = 0 WHERE id = :id")
    suspend fun markCategoryAsUpdated(id: String)

    @Query("UPDATE store SET toUpdate = 0 WHERE id = :id")
    suspend fun markStoreAsUpdated(id: String)

    @Query("UPDATE tag SET toUpdate = 0 WHERE id = :id")
    suspend fun markTagAsUpdated(id: String)

    @Query("UPDATE ProductTagCrossRef SET toUpdate = 0 WHERE id = :id")
    suspend fun markProductTagAsUpdated(id: String)

    @Query("UPDATE receipt SET toUpdate = 0 WHERE id = :id")
    suspend fun markReceiptAsUpdated(id: String)

    @Query("UPDATE product SET toUpdate = 0 WHERE id = :id")
    suspend fun markProductAsUpdated(id: String)

    @Query("UPDATE category SET toDelete = 0 WHERE id = :id")
    suspend fun markCategoryAsDeleted(id: String)

    @Query("UPDATE tag SET toDelete = 0 WHERE id = :id")
    suspend fun markTagAsDeleted(id: String)

    @Query("UPDATE productTagCrossRef SET toDelete = 0 WHERE id = :id")
    suspend fun markProductTagAsDeleted(id: String)

    @Query("UPDATE store SET toDelete = 0 WHERE id = :id")
    suspend fun markStoreAsDeleted(id: String)

    @Query("UPDATE receipt SET toDelete = 0 WHERE id = :id")
    suspend fun markReceiptAsDeleted(id: String)

    @Query("UPDATE product SET toDelete = 0 WHERE id = :id")
    suspend fun markProductAsDeleted(id: String)

    @Query("SELECT * FROM category WHERE firestoreId == ''")
    suspend fun getAllNotAddedCategories(): List<Category>

    @Query("SELECT * FROM store WHERE firestoreId == ''")
    suspend fun getAllNotAddedStore(): List<Store>

    @Query("SELECT * FROM receipt WHERE firestoreId == ''")
    suspend fun getAllNotAddedReceipt(): List<Receipt>

    @Query("SELECT * FROM product WHERE firestoreId == ''")
    suspend fun getAllNotAddedProduct(): List<Product>

    @Query("SELECT * FROM tag WHERE firestoreId == ''")
    suspend fun getAllNotAddedTag(): List<Tag>

    @Query("SELECT * FROM ProductTagCrossRef WHERE firestoreId == ''")
    suspend fun getAllNotAddedProductTag(): List<ProductTagCrossRef>

    @Query(
        "SELECT id, firestoreId, isSync, toUpdate, toDelete " +
                "FROM category " +
                "WHERE isSync == 0 AND firestoreId != '' AND toDelete == 0"
    )
    suspend fun getAllNotSyncedCategories(): List<CategoryFirebase>

    @Query(
        "SELECT id, firestoreId, isSync, toUpdate, toDelete " +
                "FROM tag " +
                "WHERE isSync == 0 AND firestoreId != '' AND toDelete == 0"
    )
    suspend fun getNotSyncedTagForFirestore(): List<TagFirebase>

    @Query(
        "SELECT  s.id, c.id as defaultCategoryId, s.firestoreId, s.isSync, c.isSync as isCategorySync, s.toUpdate, s.toDelete " +
                "FROM store s INNER JOIN category c ON s.defaultCategoryId = c.id " +
                "WHERE s.isSync = 0 AND s.firestoreId != '' AND s.toDelete == 0 "
    )
    suspend fun getNotSyncedStoreForFirestore(): List<StoreFirebase>

    @Query(
        "SELECT r.id, s.id as storeId, r.firestoreId,r.isSync,  s.isSync as isStoreSync, r.toUpdate, r.toDelete " +
                "FROM receipt r INNER JOIN store s ON r.storeId = s.id " +
                "WHERE r.isSync = 0 AND r.firestoreId != '' AND r.toDelete == 0 "
    )
    suspend fun getNotSyncedReceiptForFirestore(): List<ReceiptFirebase>

    @Query(
        "SELECT  p.id,r.id as receiptId, c.id as categoryId,p.firestoreId,p.isSync, r.isSync as isReceiptSync, c.isSync as isCategorySync, p.toUpdate, p.toDelete " +
                "FROM product p INNER JOIN receipt r ON p.receiptId = r.id " +
                "INNER JOIN category c ON p.categoryId = c.id " +
                "WHERE p.isSync = 0 AND p.firestoreId != '' AND p.toDelete == 0 "
    )
    suspend fun getNotSyncedProductForFirestore(): List<ProductFirebase>

    @Query(
        "SELECT  pt.id,p.id as productId, t.id as categoryId,pt.firestoreId,pt.isSync, p.isSync as isProductSync, t.isSync as isTagSync, pt.toUpdate, pt.toDelete " +
                "FROM ProductTagCrossRef pt INNER JOIN product p ON pt.productId = p.id " +
                "INNER JOIN tag t ON pt.tagId = t.id " +
                "WHERE pt.isSync = 0 AND pt.firestoreId != '' AND pt.toDelete == 0 "
    )
    suspend fun getNotSyncedProductTagForFirestore(): List<ProductTagCrossRefFirebase>

    @Query("SELECT * FROM category WHERE toUpdate == 1 OR toDelete == 1")
    suspend fun getAllOutdatedCategories(): List<Category>

    @Query("SELECT * FROM store WHERE toUpdate == 1 OR toDelete == 1")
    suspend fun getOutdatedStoreForFirestore(): List<Store>

    @Query("SELECT * FROM receipt WHERE toUpdate == 1 OR toDelete == 1")
    suspend fun getOutdatedReceiptForFirestore(): List<Receipt>

    @Query("SELECT * FROM product WHERE toUpdate == 1 OR toDelete == 1")
    suspend fun getOutdatedProductForFirestore(): List<Product>

    @Query("SELECT * FROM ProductTagCrossRef WHERE toUpdate == 1 OR toDelete == 1")
    suspend fun getOutdatedProductTagForFirestore(): List<ProductTagCrossRef>

    @Query("SELECT * FROM tag WHERE toUpdate == 1 OR toDelete == 1")
    suspend fun getOutdatedTagForFirestore(): List<Tag>


    @RawQuery
    suspend fun getAllStoresOrdered(query: SupportSQLiteQuery): List<Store>

    @RawQuery
    suspend fun getProductWithTag(query: SupportSQLiteQuery): List<ProductWithTag>?

    @RawQuery
    suspend fun getReceiptWithStoreOrdered(query: SupportSQLiteQuery): List<ReceiptWithStore>

    @Query("SELECT * FROM category WHERE deletedAt == '' ORDER BY name")
    suspend fun getAllCategories(): List<Category>

    @Query("SELECT * FROM tag WHERE deletedAt == '' ORDER BY name")
    suspend fun getAllTags(): List<Tag>

    @Query("SELECT * FROM productTagCrossRef WHERE deletedAt == '' ORDER BY productId")
    suspend fun getAllProductTags(): List<ProductTagCrossRef>

    @Query("SELECT * FROM category WHERE name LIKE '%'||:name||'%' AND deletedAt == '' ORDER BY name")
    suspend fun getAllCategories(name: String): List<Category>

    @Query("SELECT * FROM tag WHERE name LIKE '%'||:name||'%' AND deletedAt == '' ORDER BY name")
    suspend fun getAllTags(name: String): List<Tag>

    @Query("SELECT * FROM productTagCrossRef WHERE productId = :productId AND deletedAt == ''")
    suspend fun getAllProductTags(productId: String): List<ProductTagCrossRef>

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
            updateDependentProductByCategoryId(oldId, category.id, category.updatedAt)
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

    @Transaction
    suspend fun replaceTagWithDependencies(oldId: String) {
        val tag = getTagById(oldId)
        if (tag != null) {
            deleteTagById(oldId)
            tag.id = tag.firestoreId
            tag.updatedAt = DateUtil.getCurrentUtcTime()
            insertTag(tag)
            updateDependentProductTagByTagId(oldId, tag.id, tag.updatedAt)
        }
    }

    @Transaction
    suspend fun replaceProductWithDependencies(oldId: String) {
        val product = getProductById(oldId)
        if (product != null) {
            deleteProductById(oldId)
            product.id = product.firestoreId
            product.updatedAt = DateUtil.getCurrentUtcTime()
            insertProduct(product)
            updateDependentProductTagByProductId(oldId, product.id, product.updatedAt)
        }
    }

    @Transaction
    suspend fun replaceProductTagWithDependencies(oldId: String) {
        val productTag = getProductTagById(oldId)
        if (productTag != null) {
            deleteProductTagById(oldId)
            productTag.id = productTag.firestoreId
            productTag.updatedAt = DateUtil.getCurrentUtcTime()
            insertProductTag(productTag)
        }
    }

    @Query("DELETE FROM category WHERE id = :id")
    suspend fun deleteCategoryById(id: String)

    @Query("DELETE FROM store WHERE id = :id")
    suspend fun deleteStoreById(id: String)

    @Query("DELETE FROM receipt WHERE id = :id")
    suspend fun deleteReceiptById(id: String)

    @Query("DELETE FROM ProductTagCrossRef WHERE id = :id")
    suspend fun deleteProductTagById(id: String)

    @Query("DELETE FROM product WHERE id = :id")
    suspend fun deleteProductById(id: String)

    @Query("DELETE FROM tag WHERE id = :id")
    suspend fun deleteTagById(id: String)

    @Query("UPDATE store SET defaultCategoryId = :newCategoryId, updatedAt = :timestamp WHERE defaultCategoryId = :oldCategoryId")
    suspend fun updateDependentStoreByCategoryId(
        oldCategoryId: String,
        newCategoryId: String,
        timestamp: String
    )

    @Query("UPDATE product SET categoryId = :newCategoryId, updatedAt = :timestamp WHERE categoryId = :oldCategoryId")
    suspend fun updateDependentProductByCategoryId(
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

    @Query("UPDATE ProductTagCrossRef SET tagId = :newTagId, updatedAt = :timestamp WHERE tagId = :oldTagId")
    suspend fun updateDependentProductTagByTagId(
        oldTagId: String,
        newTagId: String,
        timestamp: String
    )

    @Query("UPDATE ProductTagCrossRef SET productId = :newProductId, updatedAt = :timestamp WHERE productId = :oldProductId")
    suspend fun updateDependentProductTagByProductId(
        oldProductId: String,
        newProductId: String,
        timestamp: String
    )

    @Query("DELETE FROM category")
    suspend fun clearCategory()

    @Query("DELETE FROM store")
    suspend fun clearStore()

    @Query("DELETE FROM receipt")
    suspend fun clearReceipt()

    @Query("DELETE FROM product")
    suspend fun clearProduct()

    @Query("DELETE FROM tag")
    suspend fun clearTag()

    @Query("DELETE FROM productTagCrossRef")
    suspend fun clearProductTag()

    @Transaction
    suspend fun deleteAllData() {
        clearProduct()
        clearReceipt()
        clearStore()
        clearCategory()
        clearTag()
        clearProductTag()
    }

}
