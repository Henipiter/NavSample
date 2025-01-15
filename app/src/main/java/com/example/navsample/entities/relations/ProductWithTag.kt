package com.example.navsample.entities.relations


data class ProductWithTag(
    var id: String,
    var tagId: String?,
    var productName: String,
    var tagName: String,
    var deletedAt: String
)