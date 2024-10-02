package com.example.navsample.dto.sort

import kotlin.reflect.KClass

data class SortProperty<Sort : ParentSort>(
    var sort: ParentSort, var direction: Direction
) {
    constructor (
        className: KClass<Sort>,
        name: String,
        direction: Direction
    ) : this(
        a(className, name), direction
    )

    override fun toString(): String {
        return sort.databaseName + " " + direction.value
    }

    companion object {
        private fun <Sort : ParentSort> a(className: KClass<Sort>, name: String): ParentSort {

            if (className == StoreSort::class) {
                return getByName(StoreSort.entries.toTypedArray(), name)
            } else if (className == ReceiptWithStoreSort::class) {
                return getByName(ReceiptWithStoreSort.entries.toTypedArray(), name)
            } else if (className == RichProductSort::class) {
                return getByName(RichProductSort.entries.toTypedArray(), name)
            }
            throw Exception("CAN'T FIND CLASS")
        }

        private fun <Sort : ParentSort> getByName(entries: Array<Sort>, fieldName: String): Sort {
            for (enum in entries) {
                if (enum.friendlyNameKey == fieldName) {
                    return enum
                }
            }
            return entries.first()
        }
    }
}