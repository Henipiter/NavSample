package com.example.navsample.dto.sort

enum class ReceiptWithStoreSort(
    override val friendlyNameKey: String,
    override val databaseName: String
) : ParentSort {
    NAME("name", "s.name"),
    PLN(" pln"),
    DATE(" date"),
    PRODUCT_COUNT("productCount");

    constructor(databaseName: String) : this(databaseName, databaseName)

}