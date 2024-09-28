package com.example.navsample.imageanalyzer

import android.util.Log
import kotlin.math.max

class ImageKeywordAnalyzer {

    companion object {
        private const val REGEX_TIME = """(\d|0\d|1\d|2[0-3])\s*:\s*[0-5]\d"""
        private const val REGEX_PRICE = """\d+\s*[,.]\s*\d\s*\d"""
        private const val REGEX_NIP_FIRST = """NIP(\.?:?)\s*(\d{10}|\d{3}-\d{2}-\d{2}-\d{3})"""
        private const val REGEX_NIP_SECOND =
            """(.IP|N.P|NI.)(\.?:?)\s*(\d{10}|\d{3}-\d{2}-\d{2}-\d{3})"""
        private const val REGEX_PLN_FIRST = """(SUMA|.UMA|S.MA|SU.A|SUM.)\s*:?\s*(PLN)\s*"""
        private const val REGEX_PLN_SECOND =
            """(SUMA|.UMA|S.MA|SU.A|SUM.)\s*:?\s*(.LN|P.N|PL.)\s*"""
        private const val REGEX_PLN_THIRD = """(SUMA|.UMA|S.MA|SU.A|SUM.)\s*"""
        private const val REGEX_PLN_FOURTH = """(SUMA|.UMA|S.MA|SU.A|SUM.)([.,:])\s*"""
        private const val REGEX_DATE =
            """(([0-2]\d|3[0-1])-(0\d|1[0-2])-20(\d\d))|(([0-2]\d|3[0-1])\.(0\d|1[0-2])\.20(\d\d))|(20(\d\d)\.(0\d|1[0-2])\.([0-2]\d|3[0-1]))|(20(\d\d)-(0\d|1[0-2])-([0-2]\d|3[0-1]))"""
    }

    private var topBoundary = 0
    private var bottomBoundary = 0
    private var leftBoundary = 0

    private var indexOfCellWithSumPrice = -1
    private var indexOfCellWithDate = -1
    private var indexOfCellWithTime = -1
    private var indexOfCellWithNip = -1

    var companyName: String = ""
    var valueTotalSum: Double = 0.0
    var valueTaxSum: Double = 0.0
    var valueDate: String = ""
    var valueTime: String = ""
    var valueNIP: String = ""


    private fun findSumKeyword(data: List<Cell>) {
        if (indexOfCellWithSumPrice == -1) {
            Log.i("ImageKeywordAnalyzer", "not found 'SUMA PLN'")
            return
        }
        val priceCell = data[indexOfCellWithSumPrice]
        specifyHorizontalBoundaries(priceCell)
        val cellsInBounds = collectPricesBetweenBoundaries(data)

        if (cellsInBounds.size >= 2) {
            valueTotalSum = cellsInBounds[0]
            valueTaxSum = cellsInBounds[1]
        }
        if (cellsInBounds.size == 1) {
            valueTotalSum = cellsInBounds[0]
        }
    }

    private fun collectPricesBetweenBoundaries(data: List<Cell>): List<Double> {
        val cellsInBounds = ArrayList<Cell>()
        data.forEach {
            if (it.getMaxY() > topBoundary && it.getMinY() < bottomBoundary && it.getMinX() > leftBoundary) {
                cellsInBounds.add(it)
            }
        }
        cellsInBounds.sortByDescending { it.getMaxY() }
        val prices = ArrayList<Double>()
        cellsInBounds.forEach {
            val foundPrice = Regex(REGEX_PRICE).find(it.content)?.value
            if (foundPrice != null) {
                prices.add(foundPrice.replace(",", ".").replace("\\s".toRegex(), "").toDouble())
            }
        }
        Log.i("ImageKeywordAnalyzer", "prices $prices")
        return prices

    }

    private fun specifyHorizontalBoundaries(cell: Cell) {
        topBoundary = max(0, cell.getMinY() - cell.getHeight() * 2)
        bottomBoundary = cell.getMaxY()
        leftBoundary = cell.getMaxX()
    }

    fun findKeywordsValues(data: List<Cell>) {
        findSumKeyword(data)
        if (indexOfCellWithDate == -1) {
            Log.i("ImageKeywordAnalyzer", "not found 'date'")
        } else {
            valueDate = Regex(REGEX_DATE).find(data[indexOfCellWithDate].content)?.value?.replace(
                "\\s".toRegex(),
                ""
            ) ?: ""
        }
        if (indexOfCellWithTime == -1) {
            Log.i("ImageKeywordAnalyzer", "not found 'time'")
        } else {
            valueTime = Regex(REGEX_TIME).find(data[indexOfCellWithTime].content)?.value?.replace(
                "\\s".toRegex(),
                ""
            ) ?: ""
        }
        if (indexOfCellWithNip == -1) {
            Log.i("ImageKeywordAnalyzer", "not found 'nip'")
        } else {
            val nip = data[indexOfCellWithNip].content
            valueNIP = Regex("""\d""").findAll(nip).take(10).joinToString("") { it.value }
        }

        Log.i("ImageKeywordAnalyzer", "PLN;$valueTotalSum")
        Log.i("ImageKeywordAnalyzer", "PTU;$valueTaxSum")
        Log.i("ImageKeywordAnalyzer", "DATE;$valueDate")
        Log.i("ImageKeywordAnalyzer", "TIME;$valueTime")
        Log.i("ImageKeywordAnalyzer", "NIP;$valueNIP")
    }

    fun findKeywordsIndexes(data: List<Cell>) {
        indexOfCellWithSumPrice = -1
        indexOfCellWithDate = -1
        indexOfCellWithTime = -1
        indexOfCellWithNip = -1
        for (i in 0..data.lastIndex) {
            if (isContainsSumPrice(data[i])) {
                indexOfCellWithSumPrice = i
            }
            if (isContainsDate(data[i])) {
                indexOfCellWithDate = i
            }
            if (isContainsTime(data[i])) {
                indexOfCellWithTime = i
            }
            if (indexOfCellWithNip == -1 && isContainsNip(data[i])) {
                indexOfCellWithNip = i
            }
        }
    }

    private fun isContainsSumPrice(cell: Cell): Boolean {
        return Regex(REGEX_PLN_FIRST).matches(cell.content) || Regex(REGEX_PLN_SECOND).matches(cell.content) ||
                Regex(REGEX_PLN_THIRD).matches(cell.content) || Regex(REGEX_PLN_FOURTH).matches(cell.content)
    }

    private fun isContainsDate(cell: Cell): Boolean {
        return Regex(REGEX_DATE).containsMatchIn(cell.content)
    }

    private fun isContainsTime(cell: Cell): Boolean {
        return Regex(REGEX_TIME).containsMatchIn(cell.content)
    }

    private fun isContainsNip(cell: Cell): Boolean {
        return Regex(REGEX_NIP_FIRST).containsMatchIn(cell.content) ||
                Regex(REGEX_NIP_SECOND).containsMatchIn(cell.content)
    }
}