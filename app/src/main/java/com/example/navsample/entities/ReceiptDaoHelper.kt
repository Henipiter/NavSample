package com.example.navsample.entities

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.navsample.dto.sort.ReceiptWithStoreSort
import com.example.navsample.dto.sort.SortProperty
import com.example.navsample.dto.sort.StoreSort
import com.example.navsample.entities.relations.ReceiptWithStore

class ReceiptDaoHelper {
    companion object {
        suspend fun getAllStoresOrdered(
            dao: ReceiptDao?,
            name: String,
            nip: String,
            orderBy: SortProperty<StoreSort>
        ): List<Store>? {

            val pattern = "SELECT * FROM store " +
                    "WHERE name LIKE '%%%s%%' " +
                    "AND nip LIKE '%%%s%%' " +
                    "order by %s %s"
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
            val pattern =
                "SELECT r.id as id, storeId, nip, s.name, s.defaultCategoryId, pln, ptu, date, time, count(p.id)  as productCount " +
                        "FROM receipt r, store s, product p " +
                        "WHERE s.id = r.storeId " +
                        "AND r.id = p.receiptId " +
                        "AND s.name LIKE '%%%s%%' " +
                        "AND r.date >= %s " +
                        "AND r.date <= %s " +
                        "GROUP BY r.id " +
                        "ORDER BY %s %s"
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

    }
}
