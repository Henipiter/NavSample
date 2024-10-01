package com.example.navsample.dto.analyzer

data class AnalyzedReceiptData(
    var companyName: String = "",
    var valueNIP: String = "",
    var valuePLN: Double = 0.0,
    var valuePTU: Double = 0.0,
    var valueDate: String = "",
    var valueTime: String = ""
)