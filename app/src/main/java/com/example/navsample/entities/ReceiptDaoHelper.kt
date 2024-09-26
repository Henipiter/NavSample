package com.example.navsample.entities

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery

class ReceiptDaoHelper {
    companion object {
        suspend fun getAllStoresOrdered(
            dao: ReceiptDao?,
            name: String,
            nip: String,
            orderBy: String,
            direction: String
        ): List<Store>? {

            val pattern = "SELECT * FROM store where " +
                    "name LIKE '%%%s%%' " +
                    "and nip LIKE '%%%s%%' " +
                    "order by %s %s"
            val sql = String.format(pattern, name, nip, orderBy, direction)
            val query: SupportSQLiteQuery = SimpleSQLiteQuery(sql)

            return dao?.getAllStoresOrdered(query)
        }

    }
}
