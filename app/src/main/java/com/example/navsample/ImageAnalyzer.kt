package com.example.navsample


import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ExperimentalGetImage
import com.example.navsample.DTO.Product
import com.example.navsample.DTO.Receipt
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.math.abs


class ImageAnalyzer {


    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    lateinit var bitmap: Bitmap
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
    private var validNIP = false
    var done = false

    var pixelNIP: Pixel? = null
    var pixelDate: Pixel? = null
    var pixelTime: Pixel? = null

    var receipt: Receipt? = null
    var productList: Array<Product> = arrayOf()

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
    fun processImageProxy(
        inputImage: InputImage,
        context: Context
    ) {
        done = false
        imageWidth = inputImage.width
//        bitmap = inputImage.bitmapInternal!!.copy(inputImage.bitmapInternal!!.config,true)
        bitmap = Bitmap.createBitmap(1500, 2000, Bitmap.Config.ARGB_8888)

        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
            }
            .addOnFailureListener {
                Log.e("ImageProcess", it.message.orEmpty())
            }.addOnCompleteListener {
                val blocks = it.result.textBlocks

                val sortedCellList = sortText(blocks)
                val lineList = convertCellsIntoString(sortedCellList).joinToString(separator = "\n")
                Log.i("ImageProcess", lineList)

                productList = getProductContent(blocks)

                findKeywords(lineList)
                findCellWithKeywords(sortedCellList)
            }

        Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show()

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
        val regexNIPfirst = """NIP(\.{0,1}:{0,1})\s*(\d{10}|.{13})"""
        val regexNIPsecond = """(.IP|N.P|NI.)(\.{0,1}:{0,1})\s*(\d{10}|.{13})"""
        val regexPTUfirst =
            """(.UMA|S.MA|SU.A|SUM.|.odatek|P.datek|Po.atek|Pod.tek|Poda.ek|Podat.k|Podate.)\s*\s*(PTU)\s*\d+[\.,]\d\d"""
        val regexPTUsecond =
            """(.UMA|S.MA|SU.A|SUM.|.odatek|P.datek|Po.atek|Pod.tek|Poda.ek|Podat.k|Podate.)\s*(.TU|P.U|PT.)\s*\d+[\.,]\d\d"""
        val regexPLNfirst =
            """(SUMA|.UMA|S.MA|SU.A|SUM.)\s*:{0,1}\s*(PLN)\s*\d+[\.,]\d\d"""
        val regexPLNsecond =
            """(SUMA|.UMA|S.MA|SU.A|SUM.)\s*:{0,1}\s*(.LN|P.N|PL.)\s*\d+[\.,]\d\d"""
        val regexDate =
            """(([0-2]\d|3[0-1])-(0\d|1[0-2])-20(\d\d))|(([0-2]\d|3[0-1])\.(0\d|1[0-2])\.20(\d\d))|(20(\d\d)\.(0\d|1[0-2])\.([0-2]\d|3[0-1]))|(20(\d\d)-(0\d|1[0-2])-([0-2]\d|3[0-1]))"""

        val regexTime = """\s+(\d|0\d|1\d|2[0-3])\s*:\s*[0-5]\d"""

        val regexPrice = """\d+\s*[,\.]\s*\d\s*\d"""

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
        valueDate = Regex(regexDate).find(lineList)?.value?.replace("\\s".toRegex(), "")
        valueTime = Regex(regexTime).find(lineList)?.value?.replace("\\s".toRegex(), "")
        validNIP = verifyNIP(valueNIP)
        companyName = lineList.split("\n")[0]

        receipt = Receipt(null, companyName, valueNIP, valuePLN, valuePTU, valueDate, valueTime)
        Log.i("ImageProcess", "valueNIP valueNIP valid: $validNIP.toString()")
        Log.i("ImageProcess", "companyName $companyName")
        Log.i("ImageProcess", "valuePTU $valuePTU")
        Log.i("ImageProcess", "valuePLN $valuePLN")
        Log.i("ImageProcess", "valueDate $valueDate")
        Log.i("ImageProcess", "valueTime $valueTime")

        done = true
    }

    private fun verifyNIP(valueNIP: String?): Boolean {
        if (valueNIP == null || !Regex("""\d{10}""").matches(valueNIP)) {
            return false
        }
        val weight = arrayOf(6, 5, 7, 2, 3, 4, 5, 6, 7)
        var sum = 0
        for (i in 0..8) {
            sum += valueNIP[i].digitToInt() * weight[i]
        }
        return sum % 11 == valueNIP[9].digitToInt()
    }

    private fun findCellWithKeywords(sortedCellList: List<Cell>) {
        Log.i("ImageProcess", "rawValueNIP" + rawValueNIP)
        Log.i("ImageProcess", "rawValuePTU" + rawValuePTU)
        Log.i("ImageProcess", "rawValuePLN" + rawValuePLN)


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
            val pixel = Pixel(cell.x1, cell.y1, cell.x2, cell.y2)
            return pixel
        }
        return null
    }

    private fun calculateMovedPoint(p: Point, alpha: Double): Point {
        val theta = alpha * Math.PI / 180.0
        return Point(
            p.x * Math.cos(theta) - p.y * Math.sin(theta),
            p.x * Math.sin(theta) + p.y * Math.cos(theta)
        )

    }

    private fun calculateBestAlpha(pointLeft: Point, pointRight: Point): Double {
        var min_distance = 1000.0
        var best_alpha = 0
        for (alpha in -45..45) {
            val vertical1 = calculateMovedPoint(pointLeft, alpha.toDouble() / 1)
            val vertical2 = calculateMovedPoint(pointRight, alpha.toDouble() / 1)
            val distance = vertical1.y - vertical2.y
            if (distance > 0 && distance < min_distance) {
                min_distance = distance
                best_alpha = alpha
            }
        }
        return best_alpha.toDouble() / 1
    }

    private fun createLineFromBlock(line: Text.Line): Line {
        return Line(
            line.text,
            Point(line.cornerPoints!!.get(0).x.toDouble(), line.cornerPoints!!.get(0).y.toDouble()),
            Point(line.cornerPoints!!.get(1).x.toDouble(), line.cornerPoints!!.get(1).y.toDouble()),
            Point(line.cornerPoints!!.get(2).x.toDouble(), line.cornerPoints!!.get(2).y.toDouble()),
            Point(line.cornerPoints!!.get(3).x.toDouble(), line.cornerPoints!!.get(3).y.toDouble())
        )
    }

    private fun rotateLine(line: Line, alpha: Double): Line {
        line.p0 = calculateMovedPoint(line.p0, alpha)
        line.p1 = calculateMovedPoint(line.p1, alpha)
        line.p2 = calculateMovedPoint(line.p2, alpha)
        line.p3 = calculateMovedPoint(line.p3, alpha)
        return line

    }

    private fun getProductContent(blocks: List<Text.TextBlock>): Array<Product> {
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


        val startKeywords =
            arrayOf("PARAGON FISKALNY", "PARAGONFISKALNY")

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
        val lengthX = (maxX - minX) / 2

        var beginReceiptCell: Line? = null
        for (line in rotatedLines) {
            for (keyword in startKeywords) {
                val data = line.data
                if (data.contains(keyword)) {
                    beginReceiptCell = line
                    break
                }
            }
        }
        if (beginReceiptCell == null) {
            return arrayOf()
        }

        val productListOnRecipe = ArrayList<Line>()
        for (line in rotatedLines) {
            if (line.p0.y > beginReceiptCell.p3.y) {
                if (line.p0.x <= minX + lengthX / 2) {
                    //NOWY PRODUKT NA PARAGONIE
                    productListOnRecipe.add(line)
                }
            }
        }
        val sortedProductListOnRecipe =
            productListOnRecipe.sortedWith(compareBy { it.p0.y }).toMutableList()
        Log.i("ImageProcess", "****************************")
//        for (product in sortedProductListOnRecipe) {
//            Log.i("ImageProcess", "( ${product.p3.x}, ${product.p3.y} ) ${product.data} ")
//        }
        //DODAJ CENY DO PROODUKTOW
        for (line in rotatedLines) {
            if (line.p0.y > beginReceiptCell.p3.y && line.p0.x > minX + lengthX / 2) {


//                Log.i("ImageProcess", "LINE ( ${line.p3.x}, ${line.p3.y} ), ${line.data} ")
                for (productIndex in 1..<sortedProductListOnRecipe.size) {
//                    Log.i(
//                        "ImageProcess",
//                        "PRODUCT ( ${sortedProductListOnRecipe[productIndex].p3.x}, ${sortedProductListOnRecipe[productIndex].p3.y} ), ${sortedProductListOnRecipe[productIndex].data} "
//                    )
                    val productNow = sortedProductListOnRecipe[productIndex]
//                    Log.i(
//                        "ImageProcess",
//                        "L${(productNow.p0.y + productNow.p3.y) / 2}>${line.p3.y}"
//                    )
                    if ((productNow.p0.y + productNow.p3.y) / 2 > line.p3.y) {
//                        Log.i(
//                            "ImageProcess",
//                            "YYYY\nLINE ( ${line.p3.y} ), ${line.data} \nPRODUCT ${sortedProductListOnRecipe[productIndex - 1].p3.y} ), ${sortedProductListOnRecipe[productIndex - 1].data}"
//                        )

                        sortedProductListOnRecipe[productIndex - 1].data += " " + line.data
                        sortedProductListOnRecipe[productIndex - 1].p1 = line.p1
                        sortedProductListOnRecipe[productIndex - 1].p2 = line.p2
                        break
                    }
                }

            }
        }
        val receiptParser = ReceiptParser()
        return receiptParser.parseToProducts(sortedProductListOnRecipe)
    }


}

