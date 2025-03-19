package com.example.navsample.dto.analyzer

data class AnalyzedReceiptData(
    var companyName: String = "",
    var valueNIP: String = "",
    var valuePLN: Int = 0,
    var valuePTU: Int = 0,
    var valueDate: String = "",
    var valueTime: String = ""
)