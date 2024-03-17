package com.example.navsample


import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import com.example.navsample.entities.Product
import com.example.navsample.imageanalyzer.DataReader
import com.example.navsample.imageanalyzer.ImageKeywordAnalyzer
import com.example.navsample.imageanalyzer.ImageProductAnalyzer
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter


class ImageAnalyzer {
var uid: String = "temp"

    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    var imageWidth = 0


    var valuePLN: Double = 0.0
    var valuePTU: Double = 0.0
    var valueDate: String = ""
    var valueTime: String = ""
    var valueNIP: String = ""
    var companyName: String = ""


    var receiptLines: ArrayList<String> = ArrayList()
    var productList: ArrayList<Product> = ArrayList()

    val imageProductAnalyzer = ImageProductAnalyzer()
    val imageKeywordAnalyzer = ImageKeywordAnalyzer()

    @ExperimentalGetImage
    fun analyzeProductList(
        inputImage: InputImage,
        receiptId: Int,
        onFinish: () -> Unit,
    ) {
        imageWidth = inputImage.width / 10

        recognizer.process(inputImage)
            .addOnSuccessListener { _ ->
            }
            .addOnFailureListener {
                Log.e("ImageProcess", it.message.orEmpty())
            }.addOnCompleteListener {
                val blocks = it.result.textBlocks
                blocksToLog(blocks, "$uid-TRIM")
                val cells = DataReader.convertContentToCells(blocks)
                imageProductAnalyzer.orderLinesByColumnContinuously(imageWidth, cells)
                imageProductAnalyzer.orderRowsInColumns()

                val receiptParser = ReceiptParser(receiptId)
                productList = receiptParser.parseToProducts(imageProductAnalyzer.productList)
                receiptLines = imageProductAnalyzer.receiptLines
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
                blocksToLog(blocks, "$uid-WHOLE")
                val cells = DataReader.convertContentToCells(blocks)
                imageKeywordAnalyzer.findKeywordsIndexes(cells)
                imageKeywordAnalyzer.findKeywordsValues(cells)

                companyName = imageKeywordAnalyzer.companyName

                valuePLN = imageKeywordAnalyzer.valueTotalSum
                valuePTU = imageKeywordAnalyzer.valueTaxSum
                valueDate = imageKeywordAnalyzer.valueDate
                valueTime = imageKeywordAnalyzer.valueTime
                valueNIP = imageKeywordAnalyzer.valueNIP

                onFinish.invoke()
            }


    }

    private fun blocksToLog(blocks: List<Text.TextBlock>, prefix: String) {
        data class ObjectItem(var text: String, var points: List<Int>) {
            constructor(text: String, pixels: Array<android.graphics.Point>?) : this(
                text,
                pixels?.flatMap { point ->
                    listOf(point.x, point.y)
                }?.toList() ?: emptyList<Int>()
            )
        }

        val lineConverted = ArrayList<ObjectItem>()
        blocks.forEach { block ->
            block.lines.forEach { line ->
                lineConverted.add(ObjectItem(line.text, line.cornerPoints))
            }
        }
        val lineOutputFile = File.createTempFile(prefix, ".txt")
        val lineOutputStream = FileOutputStream(lineOutputFile)
        val lineStreamWriter = OutputStreamWriter(lineOutputStream)


        lineStreamWriter.write(blocks[0].lines[0].angle.toString())
        lineStreamWriter.write("\n")
        lineConverted.forEach { item ->
            lineStreamWriter.write(item.points.toString())
            lineStreamWriter.write(item.text)
            lineStreamWriter.write("\n")
        }
        lineStreamWriter.close()
        lineOutputStream.close()
    }

}

