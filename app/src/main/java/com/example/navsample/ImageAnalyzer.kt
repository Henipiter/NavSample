package com.example.navsample


import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import com.example.navsample.DTO.ProductDTO
import com.example.navsample.DTO.ReceiptDTO
import com.example.navsample.DTO.StoreDTO
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.text.Normalizer
import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


class ImageAnalyzer {


    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    //    lateinit var bitmap: Bitmap
    var imageWidth = 0

    private var rawValueNIP: String? = null
    private var rawValuePTU: String? = null
    private var rawValuePLN: String? = null
    private var valueNIP: String? = null
    private var valuePTU: String? = null
    private var valuePLN: String? = null
    private var valueDate: String? = null
    private var valueTime: String? = null
    private var companyName: String? = null


    var pixelNIP: Pixel? = null
    var pixelDate: Pixel? = null
    var pixelTime: Pixel? = null

    var store: StoreDTO? = null
    var receipt: ReceiptDTO? = null
    var receiptLines: ArrayList<String> = ArrayList()
    var productList: ArrayList<ProductDTO> = ArrayList()

    data class Line(
        var data: String,
        var p0: Point,
        var p1: Point,
        var p2: Point,
        var p3: Point,
    ) {
        constructor(data: String) : this(
            data,
            Point(0.0, 0.0),
            Point(0.0, 0.0),
            Point(0.0, 0.0),
            Point(0.0, 0.0)
        )
    }

    data class Point(var x: Double, var y: Double)
    data class Cell(var data: String, var x1: Int, var y1: Int, var x2: Int, var y2: Int)
    data class Pixel(var x1: Int, var y1: Int, var x2: Int, var y2: Int)


    @ExperimentalGetImage
    fun analyzeProductList(
        inputImage: InputImage,
        onFinish: () -> Unit,
    ) {

        imageWidth = inputImage.width

        recognizer.process(inputImage)
            .addOnSuccessListener { _ ->
            }
            .addOnFailureListener {
                Log.e("ImageProcess", it.message.orEmpty())
            }.addOnCompleteListener {
                val blocks = it.result.textBlocks
                productList = getProductContent(blocks)
                onFinish.invoke()
            }
    }

    @ExperimentalGetImage
    fun analyzeReceipt(
        inputImage: InputImage,
        onFinish: () -> Unit,
    ) {
        imageWidth = inputImage.width

        recognizer.process(inputImage)
            .addOnSuccessListener { _ ->
            }
            .addOnFailureListener {
                Log.e("ImageProcess", it.message.orEmpty())
            }.addOnCompleteListener {
                val blocks = it.result.textBlocks

                val sortedCellList = sortText(blocks)
                val lineList = convertCellsIntoString(sortedCellList).joinToString(separator = "\n")
                Log.i("ImageProcess", lineList)

                findKeywords(lineList)
                findCellWithKeywords(sortedCellList)
                onFinish.invoke()
            }


    }

    private fun sortText(blocks: List<Text.TextBlock>): List<Cell> {
        val cells = mergeBlockIntoCells(blocks)
        val sortedList = cells.sortedWith(compareBy({ it.y1 }, { it.x1 })).toMutableList()

        for (i in 1..<sortedList.size) {
            val first = sortedList[i - 1]
            val second = sortedList[i]
            if (first.x1 - 100 > second.x1 && first.y1 + 40 > second.y1) {
                sortedList[i - 1] = second
                sortedList[i] = first
            }
        }
//        for (line in sortedList) {
//            Log.i("ImageProcess", line.data + "\t" + line.x + "." + line.y)
//        }
//        Log.i("ImageProcess", "=========================")
//        Log.i("ImageProcess", "=========================")
//        Log.i("ImageProcess", "=========================")
        return squashCellInSameLine(sortedList)
    }

    private fun mergeBlockIntoCells(blocks: List<Text.TextBlock>): ArrayList<Cell> {
        val cells: ArrayList<Cell> = ArrayList()
        for (block in blocks) {
            for (line in block.lines) {
                val data = line.text
                val x1 = line.cornerPoints?.get(0)?.x ?: 0
                val y1 = line.cornerPoints?.get(0)?.y ?: 0
                val x2 = line.cornerPoints?.get(2)?.x ?: 0
                val y2 = line.cornerPoints?.get(2)?.y ?: 0
                val currentCell = Cell(data, x1, y1, x2, y2)
                cells.add(currentCell)
            }
        }
        return cells
    }

    private fun squashCellInSameLine(sortedList: MutableList<Cell>): ArrayList<Cell> {
        val squashedCell = ArrayList<Cell>()

        var row: Cell? = null
        for (cell in sortedList) {
            if (abs(row?.y1?.minus(cell.y1) ?: 21) < 20 || cell.x1 > imageWidth * 0.4) {
                row?.data += " " + cell.data
                row?.x2 = cell.x2
                row?.y2 = cell.y2
            } else {
                if (row != null) {
                    squashedCell.add(row)
                }
                row = Cell(cell.data, cell.x1, cell.y1, cell.x2, cell.y2)
            }
        }
//        for (line in newSortedList) {
//            Log.i("ImageProcess", line.data + "\t" + line.x + "." + line.y)
////            Log.i("ImageProcess", line.data )
//        }
        return squashedCell
    }

    private fun convertCellsIntoString(sortedList: List<Cell>): List<String> {
        Log.i("ImageProcess", "=========================")
        Log.i("ImageProcess", "=========================")
        Log.i("ImageProcess", "=========================")
        val lines = ArrayList<String>()
        for (cell in sortedList) {
            lines.add(cell.data + " ")
        }
//        for (line in lines) {
//            Log.i("ImageProcess", line)
//        }
        return lines
    }

    private fun findKeywords(lineList: String) {
        val regexNIPfirst = """NIP(\.?:?)\s*(\d{10}|.{13})"""
        val regexNIPsecond = """(.IP|N.P|NI.)(\.?:?)\s*(\d{10}|.{13})"""
        val regexPTUfirst =
            """(.UMA|S.MA|SU.A|SUM.|.odatek|P.datek|Po.atek|Pod.tek|Poda.ek|Podat.k|Podate.)\s*\s*(PTU)\s*\d+[.,]\d\d"""
        val regexPTUsecond =
            """(.UMA|S.MA|SU.A|SUM.|.odatek|P.datek|Po.atek|Pod.tek|Poda.ek|Podat.k|Podate.)\s*(.TU|P.U|PT.)\s*\d+[.,]\d\d"""
        val regexPLNfirst =
            """(SUMA|.UMA|S.MA|SU.A|SUM.)\s*:?\s*(PLN)\s*\d+[.,]\d\d"""
        val regexPLNsecond =
            """(SUMA|.UMA|S.MA|SU.A|SUM.)\s*:?\s*(.LN|P.N|PL.)\s*\d+[.,]\d\d"""
        val regexDate =
            """(([0-2]\d|3[0-1])-(0\d|1[0-2])-20(\d\d))|(([0-2]\d|3[0-1])\.(0\d|1[0-2])\.20(\d\d))|(20(\d\d)\.(0\d|1[0-2])\.([0-2]\d|3[0-1]))|(20(\d\d)-(0\d|1[0-2])-([0-2]\d|3[0-1]))"""

        val regexTime = """\s+(\d|0\d|1\d|2[0-3])\s*:\s*[0-5]\d"""

        val regexPrice = """\d+\s*[,.]\s*\d\s*\d"""

        rawValueNIP =
            Regex(regexNIPfirst).find(lineList)?.value
                ?: Regex(regexNIPsecond).find(lineList)?.value
        valueNIP = rawValueNIP?.replace("\\s|-".toRegex(), "")
        valueNIP = valueNIP?.substring(valueNIP?.length!! - 10)?.replace("\\s".toRegex(), "")

        if (rawValuePTU == null) {
            rawValuePTU = Regex(regexPTUfirst).find(lineList)?.value
                ?: Regex(regexPTUsecond).find(lineList)?.value
            valuePTU =
                rawValuePTU?.let { Regex(regexPrice).find(it)?.value }?.replace("\\s".toRegex(), "")
        }

        if (rawValuePLN == null) {
            rawValuePLN = Regex(regexPLNfirst).find(lineList)?.value
                ?: Regex(regexPLNsecond).find(lineList)?.value
            valuePLN =
                rawValuePLN?.let { Regex(regexPrice).find(it)?.value }?.replace("\\s".toRegex(), "")
        }
        valueDate =
            Regex(regexDate).find(lineList)?.value?.replace("\\s".toRegex(), "")?.replace(".", "-")
        if (valueDate != null) {
            val splitDate = valueDate!!.split("-")
            if (splitDate[2].length == 4) {
                valueDate = splitDate[2] + "-" + splitDate[1] + "-" + splitDate[0]
            }
        }

        valueTime = Regex(regexTime).find(lineList)?.value?.replace("\\s".toRegex(), "")

        companyName = lineList.split("\n")[0]

        store = StoreDTO(companyName, valueNIP)
        receipt = ReceiptDTO(-1, companyName, valueNIP, valuePLN, valuePTU, valueDate, valueTime)
        Log.i("ImageProcess", "valueNIP $valueNIP")
        Log.i("ImageProcess", "companyName $companyName")
        Log.i("ImageProcess", "valuePTU $valuePTU")
        Log.i("ImageProcess", "valuePLN $valuePLN")
        Log.i("ImageProcess", "valueDate $valueDate")
        Log.i("ImageProcess", "valueTime $valueTime")

    }


    private fun findCellWithKeywords(sortedCellList: List<Cell>) {
        Log.i("ImageProcess", "rawValueNIP$rawValueNIP")
        Log.i("ImageProcess", "rawValuePTU$rawValuePTU")
        Log.i("ImageProcess", "rawValuePLN$rawValuePLN")


        for (cell in sortedCellList) {
            if (pixelNIP == null) {
                pixelNIP = getPixelsOfKeyword(rawValueNIP, cell)
            }
            if (pixelDate == null) {
                pixelDate = getPixelsOfKeyword(valueDate, cell)
            }
            if (pixelTime == null) {
                pixelTime = getPixelsOfKeyword(valueTime, cell)
            }

        }


    }


    private fun getPixelsOfKeyword(keyword: String?, cell: Cell): Pixel? {
        if (keyword != null && cell.data.contains(keyword.toString().trim())) {
            val x1 = cell.x1
            val y1 = cell.y1
            val x2 = cell.x2
            val y2 = cell.y2
            Log.i("ImageProcess", "( $x1, $y1 ),( $x2, $y2 ) $keyword ")
            return Pixel(cell.x1, cell.y1, cell.x2, cell.y2)
        }
        return null
    }

    private fun calculateMovedPoint(p: Point, alpha: Double): Point {
        val theta = alpha * Math.PI / 180.0
        return Point(
            p.x * cos(theta) - p.y * sin(theta),
            p.x * sin(theta) + p.y * cos(theta)
        )

    }

    private fun calculateBestAlpha(pointLeft: Point, pointRight: Point): Double {
        var minDistance = 1000.0
        var bestAlpha = 0
        for (alpha in -45..45) {
            val vertical1 = calculateMovedPoint(pointLeft, alpha.toDouble() / 1)
            val vertical2 = calculateMovedPoint(pointRight, alpha.toDouble() / 1)
            val distance = vertical1.y - vertical2.y
            if (distance > 0 && distance < minDistance) {
                minDistance = distance
                bestAlpha = alpha
            }
        }
        return bestAlpha.toDouble() / 1
    }

    private fun createLineFromBlock(line: Text.Line): Line {
        return Line(
            line.text,
            Point(line.cornerPoints!![0].x.toDouble(), line.cornerPoints!![0].y.toDouble()),
            Point(line.cornerPoints!![1].x.toDouble(), line.cornerPoints!![1].y.toDouble()),
            Point(line.cornerPoints!![2].x.toDouble(), line.cornerPoints!![2].y.toDouble()),
            Point(line.cornerPoints!![3].x.toDouble(), line.cornerPoints!![3].y.toDouble())
        )
    }

    private fun rotateLine(line: Line, alpha: Double): Line {
        line.p0 = calculateMovedPoint(line.p0, alpha)
        line.p1 = calculateMovedPoint(line.p1, alpha)
        line.p2 = calculateMovedPoint(line.p2, alpha)
        line.p3 = calculateMovedPoint(line.p3, alpha)
        return line

    }

    private fun normalizeText(text: String): String {
        val normalizedText = Normalizer.normalize(text, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(normalizedText).replaceAll("")
    }

    private fun getProductContent(blocks: List<Text.TextBlock>): ArrayList<ProductDTO> {

        val firstLine = blocks[0].lines[0]

        val pointBottomLeft = Point(
            firstLine.cornerPoints?.get(3)?.x!!.toDouble(),
            firstLine.cornerPoints?.get(3)?.y!!.toDouble()
        )
        val pointBottomRight = Point(
            firstLine.cornerPoints?.get(2)?.x!!.toDouble(),
            firstLine.cornerPoints?.get(2)?.y!!.toDouble()
        )

        val alpha = calculateBestAlpha(pointBottomLeft, pointBottomRight)


        var minX = 99999.0
        var maxX = -99999.0
        val rotatedLines = ArrayList<Line>()
        for (block in blocks) {
            for (line in block.lines) {
                val newLine = createLineFromBlock(line)
                val rotatedLine = rotateLine(newLine, alpha)
                rotatedLines.add(rotatedLine)

                if (rotatedLine.p0.x > maxX) {
                    maxX = rotatedLine.p0.x
                }
                if (rotatedLine.p3.x > maxX) {
                    maxX = rotatedLine.p3.x
                }
                if (rotatedLine.p2.x < minX) {
                    minX = rotatedLine.p0.x
                }
                if (rotatedLine.p1.x < minX) {
                    minX = rotatedLine.p3.x
                }
            }
        }
//        receiptLines = rotatedLines.map { it.data } as ArrayList<String>

        receiptLines = rotatedLines.sortedWith(compareBy { it.p0.y }).toMutableList()
            .map { it.data } as ArrayList<String>
        Log.d("ImageProcess", "EEEEEEEEEEOOOOO")
        Log.d("ImageProcess", "EEEEEEEEEEOOOOO")
        Log.d("ImageProcess", "EEEEEEEEEEOOOOO")
        Log.d("ImageProcess", "EEEEEEEEEEOOOOO")
        receiptLines.forEach { e -> Log.d("ImageProcess", e) }
        val lengthX = (maxX - minX) / 2

        //val data = normalizeText(line.data)


        val productListOnRecipe = ArrayList<Line>()
        for (line in rotatedLines) {
            if (line.p0.x <= minX + lengthX / 2) {
                //NOWY PRODUKT NA PARAGONIE
                productListOnRecipe.add(line)
            }
        }
        val sortedProductListOnRecipe =
            productListOnRecipe.sortedWith(compareBy { it.p0.y }).toMutableList()
        Log.i("ImageProcess", "****************************")
        for (product in sortedProductListOnRecipe) {
            Log.i("ImageProcess", "( ${product.p3.x}, ${product.p3.y} ) ${product.data} ")
        }
        //DODAJ CENY DO PROODUKTOW
        for (line in rotatedLines) {
            if (line.p0.x > minX + lengthX / 2) {

                Log.i("ImageProcess", "LINE ( ${line.p3.x}, ${line.p3.y} ), ${line.data} ")
                for (productIndex in 1..<sortedProductListOnRecipe.size) {
                    Log.i(
                        "ImageProcess",
                        "PRODUCT ( ${sortedProductListOnRecipe[productIndex].p3.x}, ${sortedProductListOnRecipe[productIndex].p3.y} ), ${sortedProductListOnRecipe[productIndex].data} "
                    )
                    val productNow = sortedProductListOnRecipe[productIndex]
                    Log.i(
                        "ImageProcess",
                        "L${(productNow.p0.y + productNow.p3.y) / 2}>${line.p3.y}"
                    )
                    if ((productNow.p0.y + productNow.p3.y) / 2 > line.p3.y || productIndex == sortedProductListOnRecipe.size - 1) {
                        Log.i(
                            "ImageProcess",
                            "YYYY\nLINE ( ${line.p3.y} ), ${line.data} \nPRODUCT ${sortedProductListOnRecipe[productIndex - 1].p3.y} ), ${sortedProductListOnRecipe[productIndex - 1].data}"
                        )

                        sortedProductListOnRecipe[productIndex - 1].data += " " + line.data
                        sortedProductListOnRecipe[productIndex - 1].p1 = line.p1
                        sortedProductListOnRecipe[productIndex - 1].p2 = line.p2
                        break
                    }
                }

            }
        }
        val receiptParser = ReceiptParser()
        return receiptParser.parseToProducts(sortedProductListOnRecipe.map { it.data }
            .toMutableList())
    }


}

