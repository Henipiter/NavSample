package com.example.navsample.dto.sort

enum class ReceiptWithStoreSort(override val fieldName: String) : ParentSort {
    NAME("name"),
    PLN("pln"),
    DATE("date"),
    PRODUCT_COUNT("productCount");

    override fun getByName(fieldName: String): ParentSort {
        val enumValues = ReceiptWithStoreSort.values()
        for (enum in enumValues) {
            if (enum.fieldName == fieldName) {
                return enum
            }
        }
        return enumValues.first()
    }
}