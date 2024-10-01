package com.example.navsample.dto.analyzer

import com.example.navsample.entities.Product

data class AnalyzedProductsData(
    var receiptNameLines: ArrayList<String> = ArrayList(),
    var receiptPriceLines: ArrayList<String> = ArrayList(),
    var productList: ArrayList<Product> = ArrayList()
)