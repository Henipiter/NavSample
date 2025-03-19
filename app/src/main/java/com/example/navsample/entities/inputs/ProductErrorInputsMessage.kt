package com.example.navsample.entities.inputs

class ProductErrorInputsMessage(
    var name: String? = null,
    var categoryName: String? = null,
    var quantity: String? = null,
    var unitPrice: String? = null,
    var subtotalPriceFirst: String? = null,
    var subtotalPriceSecond: String? = null,
    var discount: String? = null,
    var finalPrice: String? = null,
    var ptuType: String? = null,
    var isValidPrices: String? = null
) {
    fun isCorrect(): Boolean {
        return name == null &&
                categoryName == null &&
                quantity == null &&
                unitPrice == null &&
                subtotalPriceFirst == null &&
                subtotalPriceSecond == null &&
                discount == null &&
                finalPrice == null &&
                ptuType == null &&
                isValidPrices == null
    }
}