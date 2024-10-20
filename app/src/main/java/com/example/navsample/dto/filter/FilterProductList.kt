package com.example.navsample.dto.filter

data class FilterProductList(
    var store: String = "",
    var category: String = "",
    var lowerPrice: Int = -1,
    var higherPrice: Int = -1,
    var dateFrom: String = "",
    var dateTo: String = ""
)
