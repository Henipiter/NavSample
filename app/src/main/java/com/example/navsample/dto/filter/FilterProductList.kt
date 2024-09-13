package com.example.navsample.dto.filter

data class FilterProductList(
    var store: String,
    var category: String,
    var lowerPrice: Double,
    var higherPrice: Double,
    var dateFrom: String,
    var dateTo: String
) {
    constructor() : this("", "", -1.0, -1.0, "", "")
}
