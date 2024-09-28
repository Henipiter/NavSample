package com.example.navsample.imageanalyzer

import android.graphics.Point

data class PointBoundary(var minX: Int, var maxX: Int, var minY: Int, var maxY: Int) {
    companion object {
        fun fromPointList(points: Array<Point>): PointBoundary {
            var minX: Int = points[0].x
            var maxX: Int = points[0].x
            var minY: Int = points[0].y
            var maxY: Int = points[0].y
            for (i in 1..points.lastIndex) {
                if (points[i].x > maxX) {
                    maxX = points[i].x
                } else if (points[i].x < minX) {
                    minX = points[i].x
                }
                if (points[i].y > maxY) {
                    maxY = points[i].y
                } else if (points[i].y < minY) {
                    minY = points[i].y
                }
            }

            return PointBoundary(minX, maxX, minY, maxY)
        }
    }
}
