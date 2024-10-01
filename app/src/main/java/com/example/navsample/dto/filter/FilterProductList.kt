package com.example.navsample.dto.filter

data class FilterProductList(
    var store: String = "",
    var category: String = "",
    var lowerPrice: Double = -1.0,
    var higherPrice: Double = -1.0,
    var dateFrom: String = "",
    var dateTo: String = ""
)
