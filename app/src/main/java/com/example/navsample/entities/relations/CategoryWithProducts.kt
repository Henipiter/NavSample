package com.example.navsample.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.navsample.entities.Category
import com.example.navsample.entities.Product

data class CategoryWithProducts (
    @Embedded val category: Category,
    @Relation(
        parentColumn = "name",
        entityColumn = "categoryId"
    )
    val products: List<Product>
)