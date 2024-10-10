package com.example.navsample.entities

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.navsample.dto.sort.ReceiptWithStoreSort
import com.example.navsample.dto.sort.RichProductSort
import com.example.navsample.dto.sort.SortProperty
import com.example.navsample.dto.sort.StoreSort
import com.example.navsample.entities.relations.ProductRichData
import com.example.navsample.entities.relations.ReceiptWithStore

class ReceiptDaoHelper {
    companion object {
        suspend fun getAllStoresOrdered(
            dao: ReceiptDao?,
            name: String,
            nip: String,
            orderBy: SortProperty<StoreSort>
        ): List<Store>? {
            val sql = StringBuilder()
                .append("SELECT * ")
                .append("FROM store ")
                .append("WHERE name LIKE '%${name}%' ")
                .append("AND nip LIKE '%${nip}%' ")
                .append("ORDER BY $orderBy")
                .toString()

            val query: SupportSQLiteQuery = SimpleSQLiteQuery(sql)
            return dao?.getAllStoresOrdered(query)
        }

        suspend fun getReceiptWithStore(
            dao: ReceiptDao?,
            name: String,
            dateFrom: String,
            dateTo: String,
            orderBy: SortProperty<ReceiptWithStoreSort>
        ): List<ReceiptWithStore>? {

            val sql = StringBuilder()
                .append("SELECT r.id as id, storeId, nip, s.name, s.defaultCategoryId, pln, ptu, date, time, count(p.id)  as productCount ")
                .append("FROM receipt r ")
                .append("INNER JOIN  store s ON  s.id = r.storeId ")
                .append("LEFT JOIN product p ON  r.id = p.receiptId ")
                .append("WHERE s.name LIKE '%${name}%' ")
                .append("AND r.date >= '$dateFrom' ")
                .append("AND r.date <= '$dateTo' ")
                .append("GROUP BY r.id ")
                .append("ORDER BY $orderBy")
                .toString()
            val query: SupportSQLiteQuery = SimpleSQLiteQuery(sql)
            return dao?.getReceiptWithStoreOrdered(query)
        }

        suspend fun getAllProductsOrdered(
            dao: ReceiptDao?,
            storeName: String,
            categoryName: String,
            dateFrom: String,
            dateTo: String,
            lowerPrice: Double,
            higherPrice: Double,
            orderBy: SortProperty<RichProductSort>
        ): List<ProductRichData>? {
            val sql = StringBuilder()
                .append("SELECT s.id as storeId, s.name as storeName, r.date, c.name as categoryName, c.color as categoryColor, p.* ")
                .append("FROM product p, receipt r, store s, category c ")
                .append("WHERE p.receiptId = r.id AND s.id = r.storeId AND p.categoryId = c.id ")
                .append("AND s.name LIKE '%${storeName}%' ")
                .append("AND c.name LIKE '%${categoryName}%' ")
                .append("AND r.date >= '$dateFrom' ")
                .append("AND r.date <= '$dateTo' ")
                .append("AND p.subtotalPrice >= $lowerPrice ")
            if (higherPrice != -1.0) {
                sql.append("AND p.subtotalPrice <= $higherPrice ")
            }
            sql.append("ORDER BY $orderBy")
            val query: SupportSQLiteQuery = SimpleSQLiteQuery(sql.toString())
            return dao?.getAllProductsOrderedWithHigherPrice(query)

        }
    }

}
