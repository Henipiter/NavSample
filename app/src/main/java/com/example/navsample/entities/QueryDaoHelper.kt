package com.example.navsample.entities

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.navsample.dto.sort.ReceiptWithStoreSort
import com.example.navsample.dto.sort.RichProductSort
import com.example.navsample.dto.sort.SortProperty
import com.example.navsample.dto.sort.StoreSort

class QueryDaoHelper {
    companion object {
        fun getAllStoresOrdered(
            name: String,
            nip: String,
            orderBy: SortProperty<StoreSort>
        ): SupportSQLiteQuery {
            val sql = StringBuilder()
                .append("SELECT * ")
                .append("FROM store ")
                .append("WHERE name LIKE '%${name}%' ")
                .append("AND deletedAt == '' ")
                .append("AND nip LIKE '%${nip}%' ")
                .append("ORDER BY $orderBy")
                .toString()

            return SimpleSQLiteQuery(sql)
        }

        fun getReceiptWithStore(
            name: String,
            dateFrom: String,
            dateTo: String,
            orderBy: SortProperty<ReceiptWithStoreSort>
        ): SupportSQLiteQuery {
            val sql = StringBuilder()
                .append("SELECT r.id as id, storeId, nip, s.name, s.defaultCategoryId, pln, ptu, date, time, productPriceSum, validProductCount, productCount, r.isSync, r.toUpdate, r.toDelete ")
                .append("FROM receipt r ")
                .append("INNER JOIN  store s ON  s.id = r.storeId ")
                .append("LEFT JOIN (")
                .append("   SELECT receiptId, SUM(validPrice) AS validProductCount, SUM(finalPrice) AS productPriceSum, COUNT(id) AS productCount ")
                .append("   FROM product WHERE deletedAt = '' GROUP BY receiptId")
                .append(") p ON r.id = p.receiptId ")
                .append("WHERE s.name LIKE '%${name}%' ")
                .append("AND r.deletedAt == '' ")
                .append("AND s.deletedAt == '' ")
                .append("AND r.date >= '$dateFrom' ")
                .append("AND r.date <= '$dateTo' ")
                .append("GROUP BY r.id ")
                .append("ORDER BY $orderBy")
                .toString()
            return SimpleSQLiteQuery(sql)
        }

        fun getAllProductsOrdered(
            storeName: String,
            categoryName: String,
            dateFrom: String,
            dateTo: String,
            lowerPrice: Int,
            higherPrice: Int,
            orderBy: SortProperty<RichProductSort>
        ): SupportSQLiteQuery {
            val sql = StringBuilder()
                .append("SELECT s.id as storeId, s.name as storeName, r.date, c.name as categoryName, c.color as categoryColor, p.* ")
                .append("FROM product p, receipt r, store s, category c ")
                .append("WHERE p.receiptId = r.id AND s.id = r.storeId AND p.categoryId = c.id ")
                .append("AND p.deletedAt == '' ")
                .append("AND r.deletedAt == '' ")
                .append("AND s.deletedAt == '' ")
                .append("AND c.deletedAt == '' ")
                .append("AND s.name LIKE '%${storeName}%' ")
                .append("AND c.name LIKE '%${categoryName}%' ")
                .append("AND r.date >= '$dateFrom' ")
                .append("AND r.date <= '$dateTo' ")
                .append("AND p.subtotalPrice >= $lowerPrice ")
            if (higherPrice != -1) {
                sql.append("AND p.subtotalPrice <= $higherPrice ")
            }
            sql.append("ORDER BY $orderBy")
            return SimpleSQLiteQuery(sql.toString())
        }

        fun getProductWithTag(): SupportSQLiteQuery {
            val sql = StringBuilder()
                .append("SELECT p.id, p.name AS productName, t.name AS tagName ")
                .append("FROM ProductTagCrossRef pt ")
                .append("INNER JOIN product p ON pt.productId = p.id ")
                .append("INNER JOIN tag t ON pt.tagId = t.id ")
                .append("ORDER BY p.name")
                .toString()
            return SimpleSQLiteQuery(sql)
        }
    }

}
/*
select isValidd, * from receipt r left join (
select receiptId, sum( finalPrice) as isValidd from product where deletedAt = '' group by receiptId
) a on r.id = a.receiptId;
 */