package com.example.navsample.dto.sort

enum class RichProductSort(override val fieldName: String) : ParentSort {
    STORE_NAME("storeName"),
    PRODUCT_NAME("name"),
    DATE("date"),
    CATEGORY_NAME("categoryName"),
    DISCOUNT("discount"),
    FINAL_PRICE("finalPrice");

    override fun getByName(fieldName: String): ParentSort {
        val enumValues = RichProductSort.values()
        for (enum in enumValues) {
            if (enum.fieldName == fieldName) {
                return enum
            }
        }
        return enumValues.first()
    }


}