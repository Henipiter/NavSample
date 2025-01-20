package com.example.navsample.entities.inputs

class ProductPriceErrorInputsMessage(
    var quantityError: String? = null,
    var unitPriceError: String? = null,
    var subtotalPriceError: String? = null,
    var quantitySuggestion: String? = null,
    var unitPriceSuggestion: String? = null,
    var subtotalPriceSuggestion: String? = null,
)