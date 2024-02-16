package com.example.navsample


import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import com.example.navsample.DTO.ProductDTO
import com.example.navsample.imageanalyzer.DataReader
import com.example.navsample.imageanalyzer.ImageKeywordAnalyzer
import com.example.navsample.imageanalyzer.ImageProductAnalyzer
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions


class ImageAnalyzer {


    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    var imageWidth = 0


    var valuePLN: Double? = null
    var valuePTU: Double? = null
    var valueDate: String? = null
    var valueTime: String? = null
    var valueNIP: String? = null
    var companyName: String? = null


    var receiptLines: ArrayList<String> = ArrayList()
    var productList: ArrayList<ProductDTO> = ArrayList()

    val imageProductAnalyzer = ImageProductAnalyzer()
    val imageKeywordAnalyzer = ImageKeywordAnalyzer()

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
                val cells = DataReader.convertContentToCells(blocks)
                imageProductAnalyzer.orderLinesByColumnContinuously(imageWidth, cells)
                imageProductAnalyzer.orderRowsInColumns()


                val receiptParser = ReceiptParser()
                productList = receiptParser.parseToProducts(imageProductAnalyzer.productList)
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


}

