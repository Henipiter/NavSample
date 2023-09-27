package com.example.navsample


enum class ProductCategory(private val friendlyName: String) {
    FOOD("Food"),
    CLOTHES("Clothes"),
    OTHER("Other");

    override fun toString(): String {
        return friendlyName


    }
}
