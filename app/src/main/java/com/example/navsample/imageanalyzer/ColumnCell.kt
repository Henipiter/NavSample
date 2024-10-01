package com.example.navsample.imageanalyzer

data class ColumnCell(
    val commonColumnCells: ArrayList<Cell> = ArrayList(),
    val leftColumnCells: ArrayList<Cell> = ArrayList(),
    val rightColumnCells: ArrayList<Cell> = ArrayList(),
    val otherCells: ArrayList<Cell> = ArrayList()
)
