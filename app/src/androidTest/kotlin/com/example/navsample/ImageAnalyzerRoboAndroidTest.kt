//package com.example.navsample
//
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import com.google.mlkit.common.MlKit
//import com.google.mlkit.vision.common.InputImage
//import org.junit.Assert.assertEquals
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.robolectric.Robolectric
//import org.robolectric.RobolectricTestRunner
//import java.io.File
//
//
//@RunWith(RobolectricTestRunner::class)
////@RunWith(AndroidJUnit4::class)
////@Config(  sdk = [29])
//class ImageAnalyzerRoboAndroidTest {
//
//    @Test
//    fun clickingButton_shouldChangeMessage() {
//
//        val image = readImage(IMAGE_PATH)
//        val rotationDegrees = 0
//
//        Robolectric.buildActivity(MainActivity::class.java).use { controller ->
//            controller.setup() // Moves Activity to RESUMED state
//            MlKit.initialize(controller.get().applicationContext)
//
//            val inputImage = InputImage.fromBitmap(image, rotationDegrees)
//            val imageAnalyzer = ImageAnalyzer()
//            //when
//            imageAnalyzer.analyzeReceipt(inputImage) {}
//
//
//            assertEquals(1, imageAnalyzer.productList.size)
//        }
//
//
//    }
//
//
//    private fun readImage(filePath: String): Bitmap {
//        val file = File(filePath)
//        if (!file.exists()) {
//            throw Exception("Path is wrong")
//        }
//        return BitmapFactory.decodeFile(filePath)
//    }
//
//    companion object {
//        const val IMAGE_PATH = "C:\\Users\\henip\\Desktop\\GitHub\\NavSample\\app\\src\\test\\resources\\com\\example\\navsample\\pictures\\20230928_160448.jpg"
//
//    }
//}