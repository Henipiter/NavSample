package com.example.navsample.imageanalyzer

import android.graphics.Point
import kotlin.math.max
import kotlin.math.min


data class Cell(
    var content: String,
    var realPoint: Array<Point>,
    var calculatedPoint: Array<Point>
) {
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Cell

        if (content != other.content) return false
        if (!realPoint.contentEquals(other.realPoint)) return false
        return calculatedPoint.contentEquals(other.calculatedPoint)
    }

    override fun hashCode(): Int {
        var result = content.hashCode()
        result = 31 * result + realPoint.contentHashCode()
        result = 31 * result + calculatedPoint.contentHashCode()
        return result
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
    }
}
