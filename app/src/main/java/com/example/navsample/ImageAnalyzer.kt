package com.example.navsample


import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
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
    var valueNIP: String? = null
    var valuePTU: String? = null
    var valuePLN: String? = null
    var valueDate: String? = null
    var valueTime: String? = null
    var companyName: String? = null
    var validNIP = false

    var pixelCompanyName: Pixel? = null
    var pixelNIP: Pixel? = null
    var pixelPTU: Pixel? = null
    var pixelPLN: Pixel? = null
    var pixelDate: Pixel? = null
    var pixelTime: Pixel? = null

    data class Cell(var data: String, var x1: Int, var y1: Int, var x2: Int, var y2: Int)
    data class Pixel(var x1: Int, var y1: Int, var x2: Int, var y2: Int)
    @ExperimentalGetImage
    fun processImageProxy(
        inputImage: InputImage
    ): String? {
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


        return readText
    }
    private fun findKeywords(lineList: String) {
        val regexNIPfirst = """NIP(\.{0,1}:{0,1})\s*([0-9]{10}|.{13})"""
        val regexNIPsecond = """(.IP|N.P|NI.)(\.{0,1}:{0,1})\s*([0-9]{10}|.{13})"""
        val regexPTUfirst =
            """(.UMA|S.MA|SU.A|SUM.|.odatek|P.datek|Po.atek|Pod.tek|Poda.ek|Podat.k|Podate.)\s*(PTU)\s*[0-9]+[\.,][0-9][0-9]"""
        val regexPTUsecond =
            """(.UMA|S.MA|SU.A|SUM.|.odatek|P.datek|Po.atek|Pod.tek|Poda.ek|Podat.k|Podate.)\s*(.TU|P.U|PT.)\s*[0-9]+[\.,][0-9][0-9]"""
        val regexPLNfirst = """(SUMA|.UMA|S.MA|SU.A|SUM.):"{0,1}\s*(PLN)\s*[0-9]+[\.,][0-9][0-9]"""
        val regexPLNsecond =
            """(SUMA|.UMA|S.MA|SU.A|SUM.):{0,1}\s*(.LN|P.N|PL.)\s*[0-9]+[\.,][0-9][0-9]"""
        val regexDate =
            """(([0-2][0-9]|3[0-1])-(0[0-9]|1[0-2])-20([0-9][0-9]))|(([0-2][0-9]|3[0-1])\.(0[0-9]|1[0-2])\.20([0-9][0-9]))|(20([0-9][0-9])\.(0[0-9]|1[0-2])\.([0-2][0-9]|3[0-1]))|(20([0-9][0-9])-(0[0-9]|1[0-2])-([0-2][0-9]|3[0-1]))"""

        val regexTime = """\s+([0-9]|0[0-9]|1[0-9]|2[0-3])\s*:\s*[0-5][0-9]"""

        val regexPrice = """[0-9]+[\.,][0-9][0-9]"""
        val regexNumber = """.{10}"""

        rawValueNIP =
            Regex(regexNIPfirst).find(lineList)?.value
                ?: Regex(regexNIPsecond).find(lineList)?.value
        valueNIP = rawValueNIP?.replace("\\s|-".toRegex(), "")
        valueNIP = valueNIP?.substring(valueNIP?.length!! - 10)

        rawValuePTU = Regex(regexPTUfirst).find(lineList)?.value
            ?: Regex(regexPTUsecond).find(lineList)?.value
        valuePTU = rawValuePTU?.let { Regex(regexPrice).find(it)?.value }

        rawValuePLN = Regex(regexPLNfirst).find(lineList)?.value
            ?: Regex(regexPLNsecond).find(lineList)?.value
        valuePLN = rawValuePLN?.let { Regex(regexPrice).find(it)?.value }
        valueDate = Regex(regexDate).find(lineList)?.value
        valueTime = Regex(regexTime).find(lineList)?.value?.replace("\\s".toRegex(), "")
        validNIP = verifyNIP(valueNIP)
        companyName = lineList.split("\n")[0]

        Log.i("ImageProcess", "valueNIP valueNIP valid: $validNIP.toString()")
        Log.i("ImageProcess", "companyName $companyName")
        Log.i("ImageProcess", "valuePTU $valuePTU")
        Log.i("ImageProcess", "valuePLN $valuePLN")
        Log.i("ImageProcess", "valueDate $valueDate")
        Log.i("ImageProcess", "valueTime $valueTime")
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

    private fun getValueAfterKeyword(line: String, keywords: String): String? {
        val index = line.indexOf(keywords, 0, true)
        if (index > -1) {
            val split = line.split(" ")
            if (split[0] == keywords && (split[1].contains(",") || split[1].contains("."))) {
                return split[1]
            }
        }
        return null
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
        return  null
    }

    private fun getProductContent(line: String): String {
        val startKeywords = arrayOf("PARAGON FISKALNY", "PARAGONFISKALNY", "PARAGON", "FISKALNY")
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

    private fun sortText(blocks: List<Text.TextBlock>): List<Cell> {
        val cells: ArrayList<Cell> = ArrayList()
        for (block in blocks) {
            for (line in block.lines) {
                val data = line.text
                val x1 = line.cornerPoints?.get(0)?.x ?: 0
                val y1 = line.cornerPoints?.get(0)?.y ?: 0
                val x2 = line.cornerPoints?.get(2)?.x ?: 0
                val y2 = line.cornerPoints?.get(2)?.y ?: 0
                cells.add(Cell(data, x1, y1, x2, y2))
            }
        }
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
        val newSortedList = ArrayList<Cell>()

        var row: Cell? = null
        for (cell in sortedList) {

            if (abs(row?.y1?.minus(cell.y1) ?: 21) < 20 || cell.x1 > imageWidth * 0.4) {
                row?.data += " " + cell.data
                row?.x2 = cell.x2
                row?.y2 = cell.y2
            } else {
                if (row != null) {
                    newSortedList.add(row)
                }
                row = Cell(cell.data, cell.x1, cell.y1, cell.x2, cell.y2)
            }

        }
//        for (line in newSortedList) {
//            Log.i("ImageProcess", line.data + "\t" + line.x + "." + line.y)
////            Log.i("ImageProcess", line.data )
//        }
        return newSortedList

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

}