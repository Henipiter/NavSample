package com.example.navsample.dto.sort

enum class StoreSort(override val fieldName: String) : ParentSort {
    NAME("name"),
    NIP("nip");

    override fun getByName(fieldName: String): ParentSort {
        val enumValues = StoreSort.values()
        for (enum in enumValues) {
            if (enum.fieldName == fieldName) {
                return enum
            }
        }
        return enumValues.first()
    }
}