package com.example.navsample.imageanalyzer

import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognizer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class ImageAnalyzerTest {

    private lateinit var textRecognizer: TextRecognizer
    private lateinit var inputImage: InputImage
    private lateinit var imageAnalyzer: ImageAnalyzer

    @BeforeEach
    fun setUp() {
        textRecognizer = mock(TextRecognizer::class.java)
        inputImage = mock(InputImage::class.java)
        imageAnalyzer = ImageAnalyzer("id", textRecognizer)

    }

    @Test
    fun analyzeProductList() {
        //given

        val mockTask: Task<Text> = mock(Task::class.java) as Task<Text>


        `when`(textRecognizer.process(any(InputImage::class.java))).thenReturn(mockTask)


        //when
        imageAnalyzer.analyzeProductList(inputImage, "receiptId", "categoryId") {}


    }
}