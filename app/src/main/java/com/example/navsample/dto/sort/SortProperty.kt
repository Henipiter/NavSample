package com.example.navsample.dto.sort

data class SortProperty<Sort : ParentSort>(
    var sort: Sort, var direction: Direction
) {
    override fun toString(): String {
        return sort.fieldName + " " + direction.value
    }
}