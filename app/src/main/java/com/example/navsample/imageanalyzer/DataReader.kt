package com.example.navsample.imageanalyzer

import android.graphics.Point
import com.google.mlkit.vision.text.Text
import java.text.Normalizer
import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class DataReader {

    companion object {

        fun convertContentToCells(blocks: List<Text.TextBlock>): List<Cell> {
            val cells = arrayListOf<Cell>()
            val angle = try {
                blocks[0].lines[0].angle
            } catch (e: Exception) {
                0.0
            }.toFloat()


            for (block in blocks) {
                for (line in block.lines) {
                    val content = normalizeText(line.text)
                    if (content.replace("\\s".toRegex(), "").length > 1) {
                        var realPoint: Array<Point>
                        line.cornerPoints.let {
                            realPoint = arrayOf(
                                it?.get(0)?.let { it1 -> calculateMovedPoint(it1, angle) }
                                    ?: Point(),
                                it?.get(1)?.let { it1 -> calculateMovedPoint(it1, angle) }
                                    ?: Point(),
                                it?.get(2)?.let { it1 -> calculateMovedPoint(it1, angle) }
                                    ?: Point(),
                                it?.get(3)?.let { it1 -> calculateMovedPoint(it1, angle) }
                                    ?: Point()
                            )
                        }
                        val boundaries = PointBoundary.fromPointList(realPoint)

                        var calculatedPoint: Array<Point>
                        realPoint.let {
                            calculatedPoint = arrayOf(
                                Point(boundaries.minX, boundaries.minY),
                                Point(boundaries.maxX, boundaries.minY),
                                Point(boundaries.maxX, boundaries.maxY),
                                Point(boundaries.minX, boundaries.maxY)
                            )
                        }

                        val newCell = Cell(content, realPoint, calculatedPoint)
                        cells.add(newCell)
                    }
                }
            }
            return cells
        }

        private fun calculateMovedPoint(
            point: Point, angle: Float
        ): Point {
            val theta = Math.toRadians(abs(angle).toDouble())
            val point1 = Point(
                (point.x * cos(theta) - point.y * sin(theta)).toInt(),
                (point.x * sin(theta) + point.y * cos(theta)).toInt()
            )
            return point1

        }

        private fun normalizeText(text: String): String {
            val normalizedText = Normalizer.normalize(text, Normalizer.Form.NFD)
            val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
            return pattern.matcher(normalizedText).replaceAll("")
        }
    }

}
