package com.example.navsample.dto.sort

interface ParentSort {
    val fieldName: String

    fun getByName(fieldName: String): ParentSort
}