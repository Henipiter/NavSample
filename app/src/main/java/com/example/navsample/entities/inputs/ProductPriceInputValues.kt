package com.example.navsample.entities.inputs

class ProductPriceInputValues(
    var isSubtotalPriceBlank: Boolean = false,
    var isUnitPriceBlank: Boolean = false,
    var isQuantityBlank: Boolean = false,
    var subtotalPriceValue: Int = 1,
    var unitPriceValue: Int = 1,
    var quantityValue: Int = 1,
) {
    fun countFill(): Int {
        return arrayOf(isSubtotalPriceBlank, isUnitPriceBlank, isQuantityBlank).count { !it }
    }
}