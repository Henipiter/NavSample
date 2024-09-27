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

        private const val STORE_FROM = "FROM store "
        private const val STORE_SELECT = "SELECT * "

        private const val RECEIPT_SELECT =
            "SELECT r.id as id, storeId, nip, s.name, s.defaultCategoryId, pln, ptu, date, time, count(p.id)  as productCount "
        private const val RECEIPT_FROM = "FROM receipt r, store s, product p "

        private const val PRODUCT_SELECT =
            "SELECT s.id as storeId, s.name as storeName, r.date, c.name as categoryName, c.color as categoryColor, p.* "
        private const val PRODUCT_FROM = "FROM product p, receipt r, store s, category c "

        private const val ORDER_BY_PATTERN = "ORDER BY %s %s"

        suspend fun getAllStoresOrdered(
            dao: ReceiptDao?,
            name: String,
            nip: String,
            orderBy: SortProperty<StoreSort>
        ): List<Store>? {
            val pattern = STORE_SELECT + STORE_FROM +
                    "WHERE name LIKE '%%%s%%' " +
                    "AND nip LIKE '%%%s%%' " +
                    ORDER_BY_PATTERN
            val sql =
                String.format(pattern, name, nip, orderBy.sort.fieldName, orderBy.direction.value)
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
            val pattern = RECEIPT_SELECT +
                    RECEIPT_FROM +
                    "WHERE s.id = r.storeId " +
                    "AND r.id = p.receiptId " +
                    "AND s.name LIKE '%%%s%%' " +
                    "AND r.date >= %s " +
                    "AND r.date <= %s " +
                    "GROUP BY r.id " +
                    ORDER_BY_PATTERN
            val sql = String.format(
                pattern,
                name,
                dateFrom,
                dateTo,
                orderBy.sort.fieldName,
                orderBy.direction.value
            )
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
            var sql = ""
            var pattern = PRODUCT_SELECT +
                    PRODUCT_FROM +
                    "WHERE p.receiptId = r.id AND s.id = r.storeId AND p.categoryId = c.id " +
                    "AND s.name LIKE '%%%s%%' " +
                    "AND c.name LIKE '%%%s%%' " +
                    "AND r.date >= %s " +
                    "AND r.date <= %s " +
                    "AND p.subtotalPrice >= %s "
            if (higherPrice != -1.0) {
                pattern += "AND p.subtotalPrice <= %s "
                pattern += ORDER_BY_PATTERN
                sql = String.format(
                    pattern, storeName, categoryName, dateFrom, dateTo,
                    lowerPrice, higherPrice, orderBy.sort.fieldName, orderBy.direction.value
                )
            } else {
                pattern += ORDER_BY_PATTERN
                sql = String.format(
                    pattern, storeName, categoryName, dateFrom,
                    dateTo, lowerPrice, orderBy.sort.fieldName, orderBy.direction.value
                )
            }

            val query: SupportSQLiteQuery = SimpleSQLiteQuery(sql)
            return dao?.getAllProductsOrderedWithHigherPrice(query)

        }
    }

}
