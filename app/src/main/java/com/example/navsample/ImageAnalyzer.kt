package com.example.navsample


import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ExperimentalGetImage
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

    var biggestCellWithPLN: Cell? = null
    var cellAbovePLN: Cell? = null
    var pixelCompanyName: Pixel? = null
    var pixelNIP: Pixel? = null
    var pixelPTU: Pixel? = null
    var pixelPLN: Pixel? = null
    var pixelDate: Pixel? = null
    var pixelTime: Pixel? = null

    var receipt: Receipt? = null

    data class Cell(var data: String, var x1: Int, var y1: Int, var x2: Int, var y2: Int)
    data class Pixel(var x1: Int, var y1: Int, var x2: Int, var y2: Int)

    @ExperimentalGetImage
    fun processImageProxy(
        inputImage: InputImage,
        context: Context
    ): String? {
        done = false
        imageWidth = inputImage.width
//        bitmap = inputImage.bitmapInternal!!.copy(inputImage.bitmapInternal!!.config,true)
        bitmap = Bitmap.createBitmap(1500, 2000, Bitmap.Config.ARGB_8888)
        var readText: String? = null
        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                readText = visionText.text
            }
            .addOnFailureListener {
                Log.e("ImageProcess", it.message.orEmpty())
            }.addOnCompleteListener {
                val blocks = it.result.textBlocks

                val sortedCellList = sortText(blocks)
                val lineList = convertCellsIntoString(sortedCellList).joinToString(separator = "\n")
                Log.i("ImageProcess", lineList)
                val productContent = getProductContent(lineList)

                Log.i("ImageProcess", productContent)
                findKeywords(lineList)
                findCellWithKeywords(sortedCellList)
            }

        Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show()
        return readText
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
        var maxHeightOnRightSide = 0
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

                if (x1 > imageWidth * 0.4 && Regex("""[0-9]+\s*[,\.]\s*[0-9]\s*[0-9]""").matches(data)
                    && y2 - y1 > maxHeightOnRightSide
                ){

                    maxHeightOnRightSide = y2 - y1
                    biggestCellWithPLN = currentCell
                    biggestCellWithPLN?.data = data.replace("\\s".toRegex(), "")
                    Log.i(
                        "ImageProcess",
                        "biggestCellWithPLN" + biggestCellWithPLN!!.data + "=" + biggestCellWithPLN!!.y1 + "|"+maxHeightOnRightSide
                    )
                }
            }
        }
        findCellAbovePLN(cells)
        if (biggestCellWithPLN != null) {
            Log.i(
                "ImageProcess",
                "biggestCellWithPLN" + biggestCellWithPLN!!.data + "\t" + biggestCellWithPLN!!.x1 + "." + biggestCellWithPLN!!.y1
            )
            valuePLN = biggestCellWithPLN!!.data
            rawValuePLN = biggestCellWithPLN!!.data
            pixelPLN = Pixel(
                biggestCellWithPLN!!.x1, biggestCellWithPLN!!.y1, biggestCellWithPLN!!.x2,
                biggestCellWithPLN!!.y2
            )
        }
        if (cellAbovePLN != null) {
            Log.i(
                "ImageProcess",
                "cellAbovePLN" + cellAbovePLN!!.data + "\t" + cellAbovePLN!!.x1 + "." + cellAbovePLN!!.y1
            )

            valuePTU = cellAbovePLN!!.data
            rawValuePTU = cellAbovePLN!!.data
            pixelPTU = Pixel(
                cellAbovePLN!!.x1, cellAbovePLN!!.y1, cellAbovePLN!!.x2, cellAbovePLN!!.y2
            )
        }
        return cells
    }

    private fun findCellAbovePLN(cells: List<Cell>) {
        val yLimit = biggestCellWithPLN?.y1
        var currentY = 0

        for (cell in cells) {
            val result = Regex("""[0-9]+\s*[,\.]\s*[0-9]\s*[0-9]""").find(cell.data)?.value?.replace("\\s".toRegex(), "")

            Log.i("ImageProcess", "E0" + cell.data + "("+cell.x1)
            if(cell.x1 > imageWidth * 0.4 && result != null) {
                Log.i("ImageProcess", "E1" + result)
                if(result != biggestCellWithPLN?.data){
                    Log.i("ImageProcess", "E2" + result + cell.x1)

                }
            }


            if (cell.x1 > imageWidth * 0.4 && result != null && result != biggestCellWithPLN?.data
                && cell.y1 > currentY && cell.y1 < yLimit!!
            ) {
                currentY = cell.y1
                cellAbovePLN = cell
                cellAbovePLN?.data = result
            }
        }
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
        val regexNIPfirst = """NIP(\.{0,1}:{0,1})\s*([0-9]{10}|.{13})"""
        val regexNIPsecond = """(.IP|N.P|NI.)(\.{0,1}:{0,1})\s*([0-9]{10}|.{13})"""
        val regexPTUfirst =
            """(.UMA|S.MA|SU.A|SUM.|.odatek|P.datek|Po.atek|Pod.tek|Poda.ek|Podat.k|Podate.)\s*\s*(PTU)\s*[0-9]+[\.,][0-9][0-9]"""
        val regexPTUsecond =
            """(.UMA|S.MA|SU.A|SUM.|.odatek|P.datek|Po.atek|Pod.tek|Poda.ek|Podat.k|Podate.)\s*(.TU|P.U|PT.)\s*[0-9]+[\.,][0-9][0-9]"""
        val regexPLNfirst =
            """(SUMA|.UMA|S.MA|SU.A|SUM.)\s*:{0,1}\s*(PLN)\s*[0-9]+[\.,][0-9][0-9]"""
        val regexPLNsecond =
            """(SUMA|.UMA|S.MA|SU.A|SUM.)\s*:{0,1}\s*(.LN|P.N|PL.)\s*[0-9]+[\.,][0-9][0-9]"""
        val regexDate =
            """(([0-2][0-9]|3[0-1])-(0[0-9]|1[0-2])-20([0-9][0-9]))|(([0-2][0-9]|3[0-1])\.(0[0-9]|1[0-2])\.20([0-9][0-9]))|(20([0-9][0-9])\.(0[0-9]|1[0-2])\.([0-2][0-9]|3[0-1]))|(20([0-9][0-9])-(0[0-9]|1[0-2])-([0-2][0-9]|3[0-1]))"""

        val regexTime = """\s+([0-9]|0[0-9]|1[0-9]|2[0-3])\s*:\s*[0-5][0-9]"""

        val regexPrice = """[0-9]+\s*[,\.]\s*[0-9]\s*[0.9]"""

        rawValueNIP =
            Regex(regexNIPfirst).find(lineList)?.value
                ?: Regex(regexNIPsecond).find(lineList)?.value
        valueNIP = rawValueNIP?.replace("\\s|-".toRegex(), "")
        valueNIP = valueNIP?.substring(valueNIP?.length!! - 10)

        if (rawValuePTU == null) {
            rawValuePTU = Regex(regexPTUfirst).find(lineList)?.value
                ?: Regex(regexPTUsecond).find(lineList)?.value
            valuePTU = rawValuePTU?.let { Regex(regexPrice).find(it)?.value }
        }

        if (rawValuePLN == null) {
            rawValuePLN = Regex(regexPLNfirst).find(lineList)?.value
                ?: Regex(regexPLNsecond).find(lineList)?.value
            valuePLN = rawValuePLN?.let { Regex(regexPrice).find(it)?.value }
        }
        valueDate = Regex(regexDate).find(lineList)?.value
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
        if (valueNIP == null || !Regex("""[0-9]{10}""").matches(valueNIP)) {
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
            pixelCompanyName = getPixelsOfKeyword(companyName, cell)
            pixelNIP = getPixelsOfKeyword(rawValueNIP, cell)
            pixelPTU = getPixelsOfKeyword(rawValuePTU, cell)
            pixelPLN = getPixelsOfKeyword(rawValuePLN, cell)
            pixelDate = getPixelsOfKeyword(valueDate, cell)
            pixelTime = getPixelsOfKeyword(valueTime, cell)

        }


    }


    private fun getPixelsOfKeyword(keyword: String?, cell: Cell): Pixel? {
        if (keyword != null && cell.data.contains(keyword)) {
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

    private fun getProductContent(line: String): String {
        val startKeywords =
            arrayOf("PARAGON FISKALNY", "PARAGONFISKALNY", "PARAGON", "FISKALNY")
        val endKeywords = arrayOf("SUMA PTU", "SUMA PLN", "PTU")

        var startIndex = 0
        var endIndex = line.length
        for (startKeyword in startKeywords) {
            val index = line.indexOf(startKeyword, startIndex, true)
            if (index >= 0) {
                startIndex = index
                break
            }
        }
        for (endKeyword in endKeywords) {
            val index = line.lastIndexOf(endKeyword, endIndex, true)
            if (index >= 0) {
                endIndex = index
                break
            }
        }
        return line.substring(startIndex, endIndex)
    }


}