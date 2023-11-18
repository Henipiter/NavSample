package com.example.navsample.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.navsample.entities.Category
import com.example.navsample.entities.Product

data class ProductWithCategory(
    @Embedded val category: Category,
    @Relation(
        parentColumn = "id",
        entityColumn = "categoryId"
    )
    val products: Product
)