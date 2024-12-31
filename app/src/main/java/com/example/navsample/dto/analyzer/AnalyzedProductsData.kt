package com.example.navsample.dto.analyzer

import com.example.navsample.entities.database.Product

data class AnalyzedProductsData(
    var receiptNameLines: List<String> = ArrayList(),
    var receiptPriceLines: List<String> = ArrayList(),
    var temporaryProductList: List<Product> = ArrayList(),
    var databaseProductList: List<Product> = ArrayList()
)