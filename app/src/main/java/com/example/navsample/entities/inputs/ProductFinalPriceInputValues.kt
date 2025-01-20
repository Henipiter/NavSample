package com.example.navsample.entities.inputs

class ProductFinalPriceInputValues(
    var isSubtotalPriceBlank: Boolean = false,
    var isDiscountPriceBlank: Boolean = false,
    var isFinalPriceBlank: Boolean = false,
    var subtotalPriceValue: Int = 1,
    var discountValue: Int = 0,
    var finalPriceValue: Int = 1,
) {
    fun isAllFieldsFilled(): Boolean {
        return !isSubtotalPriceBlank && !isDiscountPriceBlank && !isFinalPriceBlank
    }

    fun isOnlyDiscountEmpty(): Boolean {
        return !isSubtotalPriceBlank && isDiscountPriceBlank && !isFinalPriceBlank
    }

    fun isOnlyFinalPriceEmpty(): Boolean {
        return !isSubtotalPriceBlank && !isDiscountPriceBlank && isFinalPriceBlank
    }

    fun isOnlySubtotalPriceEmpty(): Boolean {
        return isSubtotalPriceBlank && !isDiscountPriceBlank && !isFinalPriceBlank
    }

    fun isDiscountAndFinalPriceEmpty(): Boolean {
        return isDiscountPriceBlank && isFinalPriceBlank
    }

}