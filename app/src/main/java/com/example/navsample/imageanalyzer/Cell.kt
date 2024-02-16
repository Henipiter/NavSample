package com.example.navsample.imageanalyzer

import android.graphics.Point
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


data class Cell(
    var content: String,
    var realPoint: Array<Point>,
    var calculatedPoint: Array<Point>
) {
    constructor() : this("", arrayOf(), arrayOf())

    fun getCellWidth(): Int {
        val points = calculatedPoint.toList()
        val leftBound = min(points[0].x, points[3].x)
        val rightBound = max(points[1].x, points[2].x)
        return abs(leftBound - rightBound)
    }

    fun getMinX(): Int {
        val points = calculatedPoint.toList()
        return min(points[0].x, points[3].x)
    }

    fun getMaxX(): Int {
        val points = calculatedPoint.toList()
        return max(points[1].x, points[2].x)
    }

    fun getMinY(): Int {
        val points = calculatedPoint.toList()
        return min(points[0].y, points[1].y)
    }

    fun getMaxY(): Int {
        val points = calculatedPoint.toList()
        return max(points[2].y, points[3].y)
    }

    fun getHeight(): Int {
        return getMaxY() - getMinY()
    }

    fun joinCell(cell: Cell) {
        if (this == Cell()) {
            this.content = cell.content
            this.realPoint = cell.realPoint
            this.calculatedPoint = cell.calculatedPoint
        }
        if (cell == Cell()) {
            return
        }
        try {
            this.realPoint = arrayOf(
                this.realPoint.toList()[0], cell.realPoint.toList()[1],
                cell.realPoint.toList()[2], this.realPoint.toList()[3]
            )
            this.calculatedPoint = arrayOf(
                this.calculatedPoint.toList()[0], cell.calculatedPoint.toList()[1],
                cell.calculatedPoint.toList()[2], this.calculatedPoint.toList()[3]
            )
            this.content += " " + cell.content
        } catch (_: Exception) {
        }

    }

    companion object {
        fun sortByY(list: ArrayList<Cell>) {
            list.sortBy { it.calculatedPoint.toList()[0].y }
        }

        fun getSpaceBetween(topCell: Cell, bottomCell: Cell): Int {
            val leftSideSpace = bottomCell.realPoint.toList()[0].y - topCell.realPoint.toList()[3].y
            val rightSideSpace =
                bottomCell.realPoint.toList()[1].y - topCell.realPoint.toList()[2].y

            return min(leftSideSpace, rightSideSpace)
        }

        fun mergeCells(cell1: Cell, cell2: Cell): Cell {
            if (cell1 == Cell()) {
                return cell2
            }
            if (cell2 == Cell()) {
                return cell1
            }
            return Cell(
                cell1.content + " " + cell2.content,
                arrayOf(
                    cell1.realPoint.toList()[0], cell2.realPoint.toList()[1],
                    cell2.realPoint.toList()[2], cell1.realPoint.toList()[3]
                ), arrayOf(
                    cell1.calculatedPoint.toList()[0], cell2.calculatedPoint.toList()[1],
                    cell2.calculatedPoint.toList()[2], cell1.calculatedPoint.toList()[3]
                )
            )
        }
    }
}
