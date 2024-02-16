package com.example.navsample.imageanalyzer

import android.graphics.Point
import com.google.mlkit.vision.text.Text
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class DataReader {

    companion object {

        fun convertContentToCells(blocks: List<Text.TextBlock>): List<Cell> {
            val cells = arrayListOf<Cell>()

            for (block in blocks) {
                for (line in block.lines) {

                    val content = line.text
                    var realPoint: Array<Point>
                    line.cornerPoints.let {
                        realPoint = arrayOf(
                            it?.get(0) ?: Point(),
                            it?.get(1) ?: Point(),
                            it?.get(2) ?: Point(),
                            it?.get(3) ?: Point()
                        )
                    }

                    var calculatedPoint: Array<Point>
                    realPoint.let {
                        calculatedPoint = arrayOf(
                            calculateMovedPoint(realPoint[0], line.angle),
                            calculateMovedPoint(realPoint[1], line.angle),
                            calculateMovedPoint(realPoint[2], line.angle),
                            calculateMovedPoint(realPoint[3], line.angle)
                        )
                    }

                    val newCell = Cell(content, realPoint, calculatedPoint)
                    cells.add(newCell)
                }
            }
            return cells
        }

        private fun calculateMovedPoint(point: Point, angle: Float): Point {
            val theta = Math.toRadians(abs(angle).toDouble())
            return Point(
                (point.x * cos(theta) - point.y * sin(theta)).toInt(),
                (point.x * sin(theta) + point.y * cos(theta)).toInt()
            )

        }
    }

}