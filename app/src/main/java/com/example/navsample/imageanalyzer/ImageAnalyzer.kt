package com.example.navsample.imageanalyzer


import com.example.navsample.dto.analyzer.AnalyzedProductsData
import com.example.navsample.dto.analyzer.AnalyzedReceiptData
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter


class ImageAnalyzer(private var uid: String) {


    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var imageWidth = 0

    private val imageProductAnalyzer = ImageProductAnalyzer()
    private val imageKeywordAnalyzer = ImageKeywordAnalyzer()

    fun analyzeProductList(
        inputImage: InputImage,
        receiptId: String,
        categoryId: String,
        onFinish: (AnalyzedProductsData) -> Unit
    ) {
        imageWidth = inputImage.width / 10

        recognizer.process(inputImage).addOnCompleteListener {
            val blocks = it.result.textBlocks
            blocksToLog(blocks, "$uid-TRIM")
            val cells = DataReader.convertContentToCells(blocks)
            imageProductAnalyzer.orderLinesByColumnContinuously(imageWidth, cells)
            imageProductAnalyzer.orderRowsInColumns()

            val receiptParser = ReceiptParser(receiptId, categoryId)

            onFinish.invoke(
                AnalyzedProductsData(
                    receiptPriceLines = imageProductAnalyzer.receiptPriceLines,
                    receiptNameLines = imageProductAnalyzer.receiptNameLines,
                    temporaryProductList = receiptParser.parseToProducts(
                        imageProductAnalyzer.receiptNameLines,
                        imageProductAnalyzer.receiptPriceLines
                    )
                )
            )
        }
    }


    fun analyzeReceipt(inputImage: InputImage, onFinish: (AnalyzedReceiptData) -> Unit) {
        imageWidth = inputImage.width

        recognizer.process(inputImage).addOnCompleteListener {
            val blocks = it.result.textBlocks
            blocksToLog(blocks, "$uid-WHOLE")
            val cells = DataReader.convertContentToCells(blocks)
            imageKeywordAnalyzer.findKeywordsIndexes(cells)
            imageKeywordAnalyzer.findKeywordsValues(cells)

            onFinish.invoke(
                AnalyzedReceiptData(
                    companyName = imageKeywordAnalyzer.companyName,
                    valuePLN = imageKeywordAnalyzer.valueTotalSum,
                    valuePTU = imageKeywordAnalyzer.valueTaxSum,
                    valueDate = imageKeywordAnalyzer.valueDate,
                    valueTime = imageKeywordAnalyzer.valueTime,
                    valueNIP = imageKeywordAnalyzer.valueNIP
                )
            )
        }
    }

    private fun blocksToLog(blocks: List<Text.TextBlock>, prefix: String) {

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

    data class ObjectItem(var text: String, var points: List<Int>) {
        constructor(text: String, pixels: Array<android.graphics.Point>?) : this(
            text,
            pixels?.flatMap { point ->
                listOf(point.x, point.y)
            }?.toList() ?: emptyList<Int>()
        )
    }
}
