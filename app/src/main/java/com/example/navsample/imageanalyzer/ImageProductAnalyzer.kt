package com.example.navsample.imageanalyzer

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class ImageProductAnalyzer {
    var columnCell = ColumnCell()
    var nameList = ArrayList<String>()
    var pricesList = ArrayList<String>()

    var productList = ArrayList<String>()
    var receiptNameLines = ArrayList<String>()
    var receiptPriceLines = ArrayList<String>()

    var leftColumnBoundary = 0
    var rightColumnBoundary = 0
    fun orderLinesByColumnContinuously(imageWidth: Int, data: List<Cell>) {
        val leftColumnPosition = ArrayList<Int>()
        val rightColumnPosition = ArrayList<Int>()
        for (cell in data) {
            leftColumnPosition.add(
                min(
                    cell.calculatedPoint.toList()[0].x,
                    cell.calculatedPoint.toList()[3].x
                )
            )
            rightColumnPosition.add(
                max(
                    cell.calculatedPoint.toList()[1].x,
                    cell.calculatedPoint.toList()[2].x
                )
            )
        }
        leftColumnPosition.sortBy { it }
        rightColumnPosition.sortByDescending { it }
        val leftColumnIncreaseStep = abs(leftColumnPosition[0] - leftColumnPosition[1])
        val rightColumnIncreaseStep = abs(rightColumnPosition[0] - rightColumnPosition[1])

        leftColumnBoundary =
            calculateBoundary(leftColumnPosition, imageWidth + leftColumnIncreaseStep)
        rightColumnBoundary =
            calculateBoundary(rightColumnPosition, imageWidth + rightColumnIncreaseStep)

        for (cell in data) {
            if (cell.calculatedPoint.toList()[0].x <= leftColumnBoundary && cell.calculatedPoint.toList()[1].x >= rightColumnBoundary) {
                columnCell.commonColumnCells.add(cell)
            } else if (cell.calculatedPoint.toList()[0].x <= leftColumnBoundary) {
                columnCell.leftColumnCells.add(cell)
            } else if (cell.calculatedPoint.toList()[1].x >= rightColumnBoundary) {
                columnCell.rightColumnCells.add(cell)
            } else {
                columnCell.otherCells.add(cell)
            }
        }
        columnCell.leftColumnCells.addAll(columnCell.commonColumnCells)
        columnCell.rightColumnCells.addAll(columnCell.commonColumnCells)
        Cell.sortByY(columnCell.commonColumnCells)
        Cell.sortByY(columnCell.leftColumnCells)
        Cell.sortByY(columnCell.rightColumnCells)

        val endIndexes = arrayListOf<Int>()
        columnCell.rightColumnCells.forEach {
            endIndexes.add(it.calculatedPoint.toList()[1].x)
        }
        endIndexes.sortBy { it }

    }

    fun orderRowsInColumns() {
        val leftColumnSpaces = calculateSpacesBetweenRows(columnCell.leftColumnCells)
        val rightColumnSpaces = calculateSpacesBetweenRows(columnCell.rightColumnCells)
        getIndexesCells(columnCell, leftColumnSpaces, rightColumnSpaces)
        joinNameAndPrice()
    }

    fun joinNameAndPrice() {
        val items = min(columnCell.leftColumnCells.size, columnCell.rightColumnCells.size)
        for (i in 0..<items) {
            val name = getCellContent(columnCell.leftColumnCells[i])
            val price = getCellContent(columnCell.rightColumnCells[i])
            productList.add(name + " " + price)
            receiptNameLines.add(name)
            receiptPriceLines.add(price)
        }

    }

    private fun getCellContent(cell: Cell): String {
        return try {
            cell.content
        } catch (e: Exception) {
            ""
        }
    }


    private fun getIndexesCells(
        columnCell: ColumnCell,
        leftColumnSpaces: ArrayList<Boolean>,
        rightColumnSpaces: ArrayList<Boolean>
    ) {
        val leftList = ArrayList<String>()
        val rightList = ArrayList<String>()
        val leftListSize = leftColumnSpaces.size + leftColumnSpaces.count { it }
        val rightListSize = rightColumnSpaces.size + rightColumnSpaces.count { it }
        val listSize = max(leftListSize, rightListSize)
        for (i in 0..listSize) {
            leftList.add("-")
            rightList.add("-")
        }
        val commonsList = getIndexesOfCommonCells(columnCell)

        leftList[0] = "0"
        var leftListIterator = 0
        var rightListIterator: Int
        if (leftColumnSpaces.size > 0 && leftColumnSpaces[0] &&
            (commonsList.isEmpty() || commonsList.size > 0 && commonsList[0][1] != 0)
        ) {
            rightList[0] = "-"
            rightList[1] = "0"
            rightListIterator = 1
        } else {
            rightList[0] = "0"
            rightListIterator = 0
        }
        var leftListIndexValue = 1
        var rightListIndexValue = 1

        for (i in 0..leftColumnSpaces.lastIndex) {
            if (leftColumnSpaces[i]) {
                leftListIterator += 2
            } else {
                leftListIterator += 1
            }
            leftList[leftListIterator] = leftListIndexValue.toString()
            leftListIndexValue += 1
        }
        for (i in 0..rightColumnSpaces.lastIndex) {
            if (rightColumnSpaces[i]) {
                rightListIterator += 2
            } else {
                rightListIterator += 1
            }
            rightList[rightListIterator] = rightListIndexValue.toString()
            rightListIndexValue += 1
        }

        println("Left  $leftList")
        println("Right $rightList")
        println("Common $commonsList")

        listReduction(leftList, rightList, commonsList)
        leftList.forEach {
            val index = it.toInt()
            nameList.add(columnCell.leftColumnCells[index].content.trim())
        }
        rightList.forEach {
            if (it == "" || it == "-") {
                pricesList.add("")
            } else {
                val indexes = it.split("+").map {
                    it.toInt()
                }
                var prices = ""
                indexes.forEach {
                    prices += " " + columnCell.rightColumnCells[it].content.trim()
                }
                pricesList.add(prices)
            }
        }
        println("Result:")
        for (i in 0..nameList.lastIndex) {
            println("PRODUCT" + i.toString() + ";" + nameList[i])
        }
        for (i in 0..pricesList.lastIndex) {
            println("PRICE" + i.toString() + ";" + pricesList[i])
        }
    }

    private fun listReduction(
        leftList: ArrayList<String>,
        rightList: ArrayList<String>,
        commonsList: ArrayList<ArrayList<Int>>
    ) {
        for (i in 0..rightList.lastIndex) {
            if (rightList[i] != "-" && commonsList.any { it[1] == rightList[i].toInt() }) {
                rightList[i] = ""
            }
        }

        for (i in 0..leftList.lastIndex) {
            if (leftList[i] == "-" && rightList[i] == "-") {
                rightList[i] = "x"
                leftList[i] = "x"
            }
            if (leftList.size > i + 1 && leftList[i + 1] == "-" && rightList[i] == "-") {
                rightList[i] = rightList[i + 1]
                rightList[i + 1] = "x"
                leftList[i + 1] = "x"
            } else if (leftList[i] == "-") {
                if (rightList[i - 1] == "") {
                    rightList[i - 1] = rightList[i]
                } else {
                    rightList[i - 1] += "+" + rightList[i]
                }
                rightList[i] = "x"
                leftList[i] = "x"
            }
        }
        for (i in leftList.lastIndex downTo 0) {
            leftList.removeIf { it == "x" }
            rightList.removeIf { it == "x" }
        }

        println("RedLeft  $leftList")
        println("RedRight $rightList")
    }

    private fun calculateSpacesBetweenRows(cells: List<Cell>): ArrayList<Boolean> {
        val columnSpaces = ArrayList<Boolean>()
        if (cells.size > 1) {
            for (i in 0 until cells.lastIndex) {
                val spaceBetween = Cell.getSpaceBetween(cells[i], cells[i + 1])
                val height = cells[i].getHeight()
                if (spaceBetween > height * 0.3) {
                    columnSpaces.add(true)
                } else {
                    columnSpaces.add(false)
                }
            }
        }
        return columnSpaces
    }

    private fun calculateBoundary(columnPosition: List<Int>, boundary: Int): Int {
        for (i in 2..columnPosition.lastIndex) {
            val difference = abs(columnPosition[i - 1] - columnPosition[i])
            print("$difference ")
            if (difference > boundary) {
                return columnPosition[i - 1]
            }
        }
        return columnPosition[0]
    }

    private fun getIndexesOfCommonCells(columnCell: ColumnCell): ArrayList<ArrayList<Int>> {
        val list = ArrayList<ArrayList<Int>>()
        columnCell.commonColumnCells.forEach {
            var leftIndex = -1
            var rightIndex = -1
            for (i in 0..max(
                columnCell.leftColumnCells.lastIndex,
                columnCell.rightColumnCells.lastIndex
            )) {
                if (i <= columnCell.leftColumnCells.lastIndex && columnCell.leftColumnCells[i] == it) {
                    leftIndex = i
                }
                if (i <= columnCell.rightColumnCells.lastIndex && columnCell.rightColumnCells[i] == it) {
                    rightIndex = i
                }
                if (leftIndex != -1 && rightIndex != -1) {
                    list.add(arrayListOf(leftIndex, rightIndex))
                    break
                }
            }
        }
        return list
    }
}
