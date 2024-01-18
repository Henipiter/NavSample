//package com.example.navsample
//
//import android.content.Context
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import androidx.test.platform.app.InstrumentationRegistry
//import com.google.mlkit.common.MlKit
//import com.google.mlkit.vision.common.InputImage
//import org.junit.Assert.assertEquals
//import org.junit.Test
//import org.junit.runner.RunWith
//import java.io.File
//import java.io.InputStream
//
//
//@RunWith(AndroidJUnit4::class)
//class ImageAnalyzerJUnitAndroidTest {
//
//
//    @Test
//    fun testTextScanner() {
//
//        val appContext = InstrumentationRegistry.getInstrumentation().context
//        val image = readImage(IMAGE_PATH, appContext)
//        val rotationDegrees = 0
//
//        MlKit.initialize(appContext)
//
//        val inputImage = InputImage.fromBitmap(image, rotationDegrees)
//        val imageAnalyzer = ImageAnalyzer()
//        //when
//        imageAnalyzer.analyzeReceipt(inputImage) {}
//
//
//        assertEquals(1, imageAnalyzer.productList.size)
//    }
//
//
////    @Test
////    fun clickingButton_shouldChangeMessage() {
////
////        val image = readImage(IMAGE_PATH)
////        val rotationDegrees = 0
////
////        Robolectric.buildActivity(MainActivity::class.java).use { controller ->
////            controller.setup() // Moves Activity to RESUMED state
////            MlKit.initialize(controller.get().applicationContext)
////
////            val inputImage = InputImage.fromBitmap(image, rotationDegrees)
////            val imageAnalyzer = ImageAnalyzer()
////            //when
////            imageAnalyzer.analyzeReceipt(inputImage) {}
////
////
////            assertEquals(1, imageAnalyzer.productList.size)
////        }
////
////
////    }
//
//
//    private fun readImage(filePath: String, context: Context): Bitmap {
//
//        val x = "C:\\Users\\henip\\Desktop\\GitHub\\NavSample\\app\\src\\test\\resources\\com\\example\\navsample\\pictures\\20230928_160448.jpg"
//        val inputStream: InputStream = context.resources.assets.open(x)
//
//        val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
//
//        inputStream.close()
//        return bitmap
//
//    }
//
//    companion object {
//        const val IMAGE_PATH =
//            "resources/com/example/navsample/pictures/20230928_160448.jpg"
//
//    }
//}