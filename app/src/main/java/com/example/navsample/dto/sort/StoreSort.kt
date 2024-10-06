package com.example.navsample.dto.sort

enum class StoreSort(
    override val friendlyNameKey: String,
    override val databaseName: String
) : ParentSort {
    NAME("name"),
    NIP("nip");

    constructor(databaseName: String) : this(databaseName, databaseName)
}