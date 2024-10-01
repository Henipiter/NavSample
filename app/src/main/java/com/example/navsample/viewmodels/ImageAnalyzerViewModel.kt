package com.example.navsample.viewmodels

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.dto.analyzer.AnalyzedProductsData
import com.example.navsample.dto.analyzer.AnalyzedReceiptData
import com.example.navsample.entities.Category
import com.example.navsample.entities.Product
import com.example.navsample.imageanalyzer.GeminiAssistant
import com.example.navsample.imageanalyzer.ImageAnalyzer
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import kotlin.math.min

class ImageAnalyzerViewModel : ViewModel() {

    var uid = ""

    val productAnalyzed = MutableLiveData<AnalyzedProductsData?>(null)
    val receiptAnalyzed = MutableLiveData<AnalyzedReceiptData?>(null)

    private fun aiProductCorrection(
        productList: ArrayList<Product>,
        categories: ArrayList<Category>?,
        onFinish: (ArrayList<Product>) -> Unit
    ) {

        val categoriesPrompt = categories?.joinToString(separator = ",") { it.name } ?: "INNE"
        val productNamesPrompt =
            productList.joinToString(separator = "\n") { if (it.name == "") "-" else it.name }
        viewModelScope.launch {
            val geminiAssistant = GeminiAssistant()

            val response = geminiAssistant.sendRequest(categoriesPrompt + "\n" + productNamesPrompt)
            val correctedProducts = parseGeminiResponse(productList, categories, response)
            onFinish.invoke(correctedProducts)
        }

    }


    private fun parseGeminiResponse(
        productList: ArrayList<Product>,
        categories: ArrayList<Category>?,
        response: String?
    ): ArrayList<Product> {
        if (response == null) {
            return productList
        }
        val responseList = response.split("\n")
        val size = min(productList.size, responseList.size)
        for (i in 0..<size) {
            Log.i("Gemini", "Product: ${productList[i]}")
            Log.i("Gemini", "Response: ${responseList[i]}")
            val split = responseList[i].split("|")
            if (split.size == 2) {

                Log.i("Gemini", "Name: '${split[0].trim()}'")
                Log.i("Gemini", "Category: '${split[1].trim()}'")
                productList[i].name = split[0].trim()
                categories?.find { it.name == split[1].trim() }?.let {
                    if (it.id != null) {
                        Log.i("Gemini", "Name: '${it.id}'")
                        productList[i].categoryId = it.id ?: 0
                    }
                }
            }
        }
        return productList
    }

    fun analyzeProductList(
        bitmap: Bitmap,
        receiptId: Int,
        categoryId: Int,
        categories: ArrayList<Category>?
    ) {
        productAnalyzed.value = null
        val imageAnalyzer = ImageAnalyzer(uid)
        viewModelScope.launch {
            imageAnalyzer.analyzeProductList(
                InputImage.fromBitmap(bitmap, 0),
                receiptId,
                categoryId
            ) { analyzedProducts ->
                productAnalyzed.value = analyzedProducts

                aiProductCorrection(analyzedProducts.productList, categories)
                {
                    analyzedProducts.productList = it
                    productAnalyzed.value = analyzedProducts
                }

            }
        }
    }


    fun analyzeReceipt(analyzedImage: InputImage) {
        receiptAnalyzed.value = null
        val imageAnalyzer = ImageAnalyzer(uid)
        viewModelScope.launch {
            imageAnalyzer.analyzeReceipt(analyzedImage) {
                receiptAnalyzed.value = it
            }
        }
    }
}