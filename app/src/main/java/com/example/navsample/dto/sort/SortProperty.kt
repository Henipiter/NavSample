package com.example.navsample.dto.sort

data class SortProperty<Sort : ParentSort>(
    var sort: Sort, var direction: Direction
)