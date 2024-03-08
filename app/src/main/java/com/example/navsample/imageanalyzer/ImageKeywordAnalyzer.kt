package com.example.navsample.imageanalyzer

import kotlin.math.max

class ImageKeywordAnalyzer {
    var topBoundary = 0
    var bottomBoundary = 0
    var leftBoundary = 0


    var indexOfCellWithSumPrice = -1
    var indexOfCellWithDate = -1
    var indexOfCellWithTime = -1
    var indexOfCellWithNip = -1

    var companyName: String = ""
    var valueTotalSum: Double = 0.0
    var valueTaxSum: Double = 0.0
    var valueDate: String = ""
    var valueTime: String = ""
    var valueNIP: String = ""


    private val regexNIPfirst = """NIP(\.?:?)\s*(\d{10}|\d{3}-\d{2}-\d{2}-\d{3})"""
    private val regexNIPsecond = """(.IP|N.P|NI.)(\.?:?)\s*(\d{10}|\d{3}-\d{2}-\d{2}-\d{3})"""

    private val regexPLNfirst =
        """(SUMA|.UMA|S.MA|SU.A|SUM.)\s*:?\s*(PLN)\s*"""
    private val regexPLNsecond =
        """(SUMA|.UMA|S.MA|SU.A|SUM.)\s*:?\s*(.LN|P.N|PL.)\s*"""
    val regexPLNthird =
        """(SUMA|.UMA|S.MA|SU.A|SUM.)\s*"""
    val regexPLNfourth =
        """(SUMA|.UMA|S.MA|SU.A|SUM.)([.,:])\s*"""
    private val regexDate =
        """(([0-2]\d|3[0-1])-(0\d|1[0-2])-20(\d\d))|(([0-2]\d|3[0-1])\.(0\d|1[0-2])\.20(\d\d))|(20(\d\d)\.(0\d|1[0-2])\.([0-2]\d|3[0-1]))|(20(\d\d)-(0\d|1[0-2])-([0-2]\d|3[0-1]))"""

    private val regexTime = """(\d|0\d|1\d|2[0-3])\s*:\s*[0-5]\d"""

    private val regexPrice = """\d+\s*[,.]\s*\d\s*\d"""


    fun findSumKeyword(data: List<Cell>) {

        if (indexOfCellWithSumPrice == -1) {
            println("not found 'SUMA PLN'")
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
            val foundPrice = Regex(regexPrice).find(it.content)?.value
            if (foundPrice != null) {
                prices.add(foundPrice.replace(",", ".").replace("\\s".toRegex(), "").toDouble())
            }
        }
        println("prices $prices")
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
            println("not found 'date'")
        } else {
            valueDate = Regex(regexDate).find(data[indexOfCellWithDate].content)?.value?.replace(
                "\\s".toRegex(),
                ""
            ) ?: ""
        }
        if (indexOfCellWithTime == -1) {
            println("not found 'time'")
        } else {
            valueTime = Regex(regexTime).find(data[indexOfCellWithTime].content)?.value?.replace(
                "\\s".toRegex(),
                ""
            ) ?: ""
        }
        if (indexOfCellWithNip == -1) {
            println("not found 'nip'")
        } else {
            val nip = data[indexOfCellWithNip].content
            valueNIP = Regex("""\d""").findAll(nip).take(10).joinToString("") { it.value }
        }

        println("PLN;$valueTotalSum")
        println("PTU;$valueTaxSum")
        println("DATE;$valueDate")
        println("TIME;$valueTime")
        println("NIP;$valueNIP")
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
        return Regex(regexPLNfirst).matches(cell.content) || Regex(regexPLNsecond).matches(cell.content) ||
                Regex(regexPLNthird).matches(cell.content) || Regex(regexPLNfourth).matches(cell.content)
    }

    private fun isContainsDate(cell: Cell): Boolean {
        return Regex(regexDate).containsMatchIn(cell.content)
    }

    private fun isContainsTime(cell: Cell): Boolean {
        return Regex(regexTime).containsMatchIn(cell.content)
    }

    private fun isContainsNip(cell: Cell): Boolean {
        return Regex(regexNIPfirst).containsMatchIn(cell.content) ||
                Regex(regexNIPsecond).containsMatchIn(cell.content)
    }
}