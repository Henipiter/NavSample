package com.example.navsample.dto.sort

enum class RichProductSort(
    override val friendlyNameKey: String,
    override val databaseName: String
) : ParentSort {
    STORE_NAME("storeName"),
    PRODUCT_NAME("name", "p.name"),
    DATE("date"),
    CATEGORY_NAME("categoryName"),
    DISCOUNT("discount"),
    FINAL_PRICE("finalPrice");

    constructor(databaseName: String) : this(databaseName, databaseName)
}