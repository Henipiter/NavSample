package com.example.navsample.entities.inputs

data class ProductInputs(
    var name: CharSequence? = null,
    var categoryName: CharSequence? = null,
    var quantity: CharSequence? = null,
    var unitPrice: CharSequence? = null,
    var subtotalPrice: CharSequence? = null,
    var discount: CharSequence? = null,
    var finalPrice: CharSequence? = null,
    var ptuType: CharSequence? = null
)