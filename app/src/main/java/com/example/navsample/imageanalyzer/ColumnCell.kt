package com.example.navsample.imageanalyzer

data class ColumnCell(
    val commonColumnCells: ArrayList<Cell>,
    val leftColumnCells: ArrayList<Cell>,
    val rightColumnCells: ArrayList<Cell>,
    val otherCells: ArrayList<Cell>
) {
    constructor() : this(ArrayList<Cell>(), ArrayList<Cell>(), ArrayList<Cell>(), ArrayList<Cell>())

}
