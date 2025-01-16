package com.example.navsample.dto

import kotlin.math.roundToInt

class PriceUtils {

    companion object {

        fun intPriceToString(integer: Int): String {
            return doubleToString(integer / 100.0)
        }

        fun intQuantityToString(integer: Int): String {
            return quantityToString(integer / 1000.0)
        }

        fun doublePriceTextToInt(text: CharSequence?): Int {
            if (text == null) {
                return 0
            }
            return doublePriceTextToInt(text.toString())
        }

        fun doublePriceTextToInt(text: String): Int {
            if (text == "null" || text == "") {
                return 0
            }
            return (text.toDouble() * 100).roundToInt()
        }

        fun doubleQuantityTextToInt(text: CharSequence?): Int {
            if (text == null) {
                return 0
            }
            return doubleQuantityTextToInt(text.toString())
        }

        fun doubleQuantityTextToInt(text: String): Int {
            if (text == "null" || text == "") {
                return 0
            }
            return (text.toDouble() * 1000).roundToInt()
        }

        fun roundInt(double: Double, multiplier: Int): Int {
            return (double * multiplier).toInt()
        }

        private fun doubleToString(double: Double?): String {
            if (double == null || double.isNaN() || double.isInfinite()) {
                return doubleToString(1.0)
            }
            return "%.2f".format(double)
        }

        private fun quantityToString(double: Double): String {
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
