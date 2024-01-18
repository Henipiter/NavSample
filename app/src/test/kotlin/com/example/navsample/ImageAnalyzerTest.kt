package com.example.navsample

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File


class ImageAnalyzerTest {


    var imageAnalyzer = ImageAnalyzer()

    @Test
    fun analyzeProductList() {
        //given
//        MlKit.initialize()
        val image = readImage(IMAGE_PATH)
        val rotationDegrees = 0
        val inputImage = InputImage.fromBitmap(image, rotationDegrees)

        //when
        imageAnalyzer.analyzeProductList(inputImage) {}

        //then
        assertAll(
            { assertEquals(0, imageAnalyzer.productList.size) }
        )
    }

    @Test
    fun analyzeReceipt() {
        //given
        val image = readImage(IMAGE_PATH)
        val rotationDegrees = 0
        val inputImage = InputImage.fromBitmap(image, rotationDegrees)

        //when
        imageAnalyzer.analyzeReceipt(inputImage) {}
    }

    private fun readImage(filePath: String): Bitmap {
        val file = File(filePath)
        if (!file.exists()) {
            throw Exception("Path is wrong")
        }
        return BitmapFactory.decodeFile(filePath)
    }

    companion object {
        const val IMAGE_PATH = "com/example/navsample/receiptimage/AUTOSERWIS.jpg"
        const val DATA_PATH = "com/example/navsample/receiptdata/AUTOSERWIS_TRIM.txt"

    }
}
