package com.example.navsample.entities

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.navsample.dto.sort.SortProperty
import com.example.navsample.dto.sort.StoreSort

class ReceiptDaoHelper {
    companion object {
        suspend fun getAllStoresOrdered(
            dao: ReceiptDao?,
            name: String,
            nip: String,
            orderBy: SortProperty<StoreSort>
        ): List<Store>? {

            val pattern = "SELECT * FROM store where " +
                    "name LIKE '%%%s%%' " +
                    "and nip LIKE '%%%s%%' " +
                    "order by %s %s"
            val sql =
                String.format(pattern, name, nip, orderBy.sort.fieldName, orderBy.direction.value)
            val query: SupportSQLiteQuery = SimpleSQLiteQuery(sql)

            return dao?.getAllStoresOrdered(query)
        }

    }
}
