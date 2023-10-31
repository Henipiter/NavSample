package com.example.navsample.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.navsample.entities.Product
import com.example.navsample.entities.Receipt

data class ReceiptWithProducts (
    @Embedded val receipt: Receipt,
    @Relation(
        parentColumn = "id",
        entityColumn = "receiptId"
    )
    val products: List<Product>
)