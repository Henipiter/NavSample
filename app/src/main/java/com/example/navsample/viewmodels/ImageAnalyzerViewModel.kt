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
    val geminiResponse = MutableLiveData<String?>(null)
    val geminiError = MutableLiveData<String?>(null)
    val isGeminiWorking = MutableLiveData(false)

    fun clearData() {
        productAnalyzed.value = null
        receiptAnalyzed.value = null
        geminiResponse.value = null
        isGeminiWorking.value = false
    }

    private fun aiProductCorrection(
        productList: ArrayList<Product>,
        categories: List<Category>?,
        onFinish: (ArrayList<Product>, String) -> Unit
    ) {
        viewModelScope.launch {
            val response = sendGeminiRequest(productList, categories)
            if (response == null || response == "") {
                isGeminiWorking.value = false
                return@launch
            }
            try {
                val correctedProducts = parseGeminiResponse(productList, categories, response)
                onFinish.invoke(correctedProducts, response)
            } catch (exception: Exception) {
                Log.e("Gemini", exception.message, exception)
                isGeminiWorking.value = false
                onFinish.invoke(productList, response)
            }
        }
    }

    private suspend fun sendGeminiRequest(
        productList: ArrayList<Product>,
        categories: List<Category>?,
    ): String? {

        val categoriesPrompt = categories?.joinToString(separator = ",") { it.name } ?: "INNE"
        val productNamesPrompt =
            productList.joinToString(separator = "\n") { if (it.name == "") "-" else it.name }

        val geminiAssistant = GeminiAssistant()
        return try {
            geminiAssistant.sendRequest(categoriesPrompt + "\n" + productNamesPrompt)
        } catch (exception: Exception) {
            Log.e("Gemini", exception.message, exception)
            geminiError.value = "Cannot get AI response. Check internet connection"
            isGeminiWorking.value = false
            null
        }
    }

    private fun parseGeminiResponse(
        productList: ArrayList<Product>,
        categories: List<Category>?,
        response: String?
    ): ArrayList<Product> {
        if (response == null) {
            return productList
        }
        val responseList = response.replace("\"", "").split("\n")
        val size = min(productList.size, responseList.size)
        for (i in 0..<size) {
            Log.i("Gemini", "Product: ${productList[i]}")
            Log.i("Gemini", "Response: ${responseList[i]}")
            val split = responseList[i].split("|")
            if (split.size == 2 && split[0] != "-") {
                Log.i("Gemini", "Name: '${split[0].trim()}'")
                Log.i("Gemini", "Category: '${split[1].trim()}'")
                updateNameAndCategory(productList, categories, i, split)
            }
        }
        return productList
    }

    private fun updateNameAndCategory(
        productList: ArrayList<Product>,
        categories: List<Category>?,
        index: Int,
        split: List<String>
    ) {
        productList[index].name = split[0].trim()
        categories?.find { it.name == split[1].trim() }?.let {
            if (it.id.isNotEmpty()) {
                Log.i("Gemini", "Name: '${it.id}'")
                productList[index].categoryId = it.id
            }
        }
    }

    fun analyzeProductList(
        bitmap: Bitmap,
        receiptId: String,
        categoryId: String,
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
                aiAnalyze(analyzedProducts, categories)

            }
        }
    }

    fun aiAnalyze(
        analyzedProducts: AnalyzedProductsData,
        categories: List<Category>?
    ) {
        productAnalyzed.postValue(analyzedProducts)
        isGeminiWorking.postValue(true)

        val aggregated = arrayListOf<Product>()
        aggregated.addAll(analyzedProducts.temporaryProductList)
        aggregated.addAll(analyzedProducts.databaseProductList)
        aiProductCorrection(aggregated, categories)
        { list, response ->
            analyzedProducts.temporaryProductList =
                list.take(analyzedProducts.temporaryProductList.size)
            analyzedProducts.databaseProductList =
                list.takeLast(analyzedProducts.databaseProductList.size)
            productAnalyzed.postValue(analyzedProducts)
            geminiResponse.postValue(response)
            isGeminiWorking.postValue(false)
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
