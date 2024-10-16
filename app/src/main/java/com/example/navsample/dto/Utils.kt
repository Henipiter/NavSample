package com.example.navsample.dto

class Utils {

    companion object {
        fun roundDouble(double: Double?): Double {
            if (double == null) {
                return 0.0
            }
            return roundDouble(double)
        }

        fun roundDouble(double: Double): Double {
            return doubleToString(double).toDouble()
        }

        fun doubleToString(double: Double?): String {
            if (double == null || double.isNaN() || double.isInfinite()) {
                return doubleToString(1.0)
            }
            return "%.2f".format(double)
        }

        fun quantityToString(double: Double): String {
            if (double.isNaN() || double.isInfinite()) {
                return doubleToString(1.0)
            }
            return if (double % 1.0 == 0.0) {
                "%.0f".format(double)
            } else {
                "%.3f".format(double)
            }
        }
    }
}
