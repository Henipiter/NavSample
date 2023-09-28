package com.example.navsample


import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.math.abs


class ImageAnalyzer {


    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    var imageWidth = 0

    data class Cell(var data: String, val x: Int, val y: Int)

    private fun findKeywords(lineList: List<String>) {
        val constPTU = "PTU"
        val constPLN = "PLN"
        val dateRegex =
            """^(?:(?:31(\\/|-|\\.)(?:0?[13578]|1[02]))\\1|(?:(?:29|30)(\\/|-|\\.)(?:0?[13-9]|1[0-2])\\2))(?:(?:1[6-9]|[2-9]\\d)?\\d{2})\$|^(?:29(\\/|-|\\.)0?2\\3(?:(?:(?:1[6-9]|[2-9]\\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))\$|^(?:0?[1-9]|1\\d|2[0-8])(\\/|-|\\.)(?:(?:0?[1-9])|(?:1[0-2]))\\4(?:(?:1[6-9]|[2-9]\\d)?\\d{2})\$"""
                .toRegex()
        val timeRegex = """^([0-1]?[0-9]|2[0-3]):[0-5][0-9]\$""".toRegex()

        var valuePTU: String? = null
        var valuePLN: String? = null
        var valueDate: String? = null
        var valueTime: String? = null
        var valuePTUIterator = 0
        var valuePLNIterator = 0
        var valueDateIterator = 0
        var valueTimeIterator = 0
        var iterator = 0
        for (line in lineList) {
            if (valuePTU == null) {
                valuePTUIterator = iterator
                valuePTU = getValueAfterKeyword(line, constPTU)
            }
            if (valuePLN == null) {
                valuePLNIterator = iterator
                valuePLN = getValueAfterKeyword(line, constPLN)
            }
            if (valueDate == null) {
                valueDateIterator = iterator
                valueDate = dateRegex.find(line, 0)?.value
            }
            if (valueTime == null) {
                valueTimeIterator = iterator
                valueTime = timeRegex.find(line, 0)?.value
            }
            iterator++
        }
        Log.i("ImageProcess", valuePTUIterator.toString() + "valuePTU" + valuePTU)
        Log.i("ImageProcess", valuePLNIterator.toString() + "valuePLN" + valuePLN)
        Log.i("ImageProcess", valueDateIterator.toString() + "valueDate" + valueDate)
        Log.i("ImageProcess", valueTimeIterator.toString() + "valueTime" + valueTime)
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


    @ExperimentalGetImage
    fun processImageProxy(
        inputImage: InputImage
    ): String? {
        imageWidth = inputImage.width
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
//                val lineList = convertCellsIntoString(sortedCellList).joinToString(separator = "\n")
//                val productContent = getProductContent(lineList)

//                Log.i("ImageProcess", productContent)
//                findKeywords(lineList)
            }


        return readText
    }

    private fun getProductContent(line: String): String {
        val startKeywords = arrayOf("PARAGON FISKALNY", "PARAGON", "FISKALNY")
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
                val x = line.cornerPoints?.get(0)?.x ?: 0
                val y = line.cornerPoints?.get(0)?.y ?: 0
                cells.add(Cell(data, x, y))
            }
        }
        val sortedList = cells.sortedWith(compareBy({ it.y }, { it.x })).toMutableList()

        for (i in 1..<sortedList.size) {
            val first = sortedList[i - 1]
            val second = sortedList[i]
            if (first.x - 100 > second.x && first.y + 40 > second.y) {
                sortedList[i - 1] = second
                sortedList[i] = first
            }

        }
        for (line in sortedList) {
            Log.i("ImageProcess", line.data + "\t" + line.x + "." + line.y)
        }
        Log.i("ImageProcess", "=========================")
        Log.i("ImageProcess", "=========================")
        Log.i("ImageProcess", "=========================")
        val newSortedList = ArrayList<Cell>()

        var row: Cell? = null
        for (cell in sortedList) {

            if (abs(row?.y?.minus(cell.y) ?: 21)<20 ||  cell.x > imageWidth * 0.4)
                row?.data += " " + cell.data
            else {
                if (row != null) {
                    newSortedList.add(row)
                }
                row = Cell(cell.data, cell.x, cell.y)
            }

        }
        for (line in newSortedList) {
//            Log.i("ImageProcess", line.data + "\t" + line.x + "." + line.y)
            Log.i("ImageProcess", line.data )
        }
        return sortedList

    }

    private fun convertCellsIntoString(sortedList: List<Cell>): List<String> {
        Log.i("ImageProcess", "=========================")
        Log.i("ImageProcess", "=========================")
        Log.i("ImageProcess", "=========================")
        var lastY = sortedList.get(0).y
        val lines = ArrayList<String>()

        var str = ""
        for (cell in sortedList) {
            if (cell.y > lastY + 60) {
                lines.add(str)
                str = ""
                lastY = cell.y
            }

            str += cell.data + " "
        }
        for (line in lines) {
            Log.i("ImageProcess", line)
        }
        Log.i("ImageProcess", "=========================")
        Log.i("ImageProcess", "=========================")
        Log.i("ImageProcess", "=========================")
        return lines
    }

}