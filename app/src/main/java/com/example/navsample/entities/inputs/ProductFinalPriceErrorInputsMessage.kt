package com.example.navsample.entities.inputs

class ProductFinalPriceErrorInputsMessage(
    var subtotalPriceError: String? = null,
    var discountError: String? = null,
    var finalPriceError: String? = null,
    var subtotalPriceSuggestion: String? = null,
    var discountSuggestion: String? = null,
    var finalPriceSuggestion: String? = null,
)