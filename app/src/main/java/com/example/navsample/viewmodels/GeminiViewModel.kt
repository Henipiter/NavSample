package com.example.navsample.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.BuildConfig
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.launch

class GeminiViewModel : ViewModel() {
    var response = MutableLiveData<String?>()


    fun sendRequest(prompt: String) {
        val model = getModel()
        val chatHistory = getChatHistory()
        val chat = model.startChat(chatHistory)
        viewModelScope.launch {
            response.postValue(sendMessage(chat, prompt))
        }
    }

    private suspend fun sendMessage(chat: Chat, prompt: String): String? {
        return chat.sendMessage(prompt).text

    }

    private fun getModel(): GenerativeModel {
        return GenerativeModel(
            "gemini-1.5-flash",
            BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 0.2f
                topK = 64
                topP = 0.95f
                maxOutputTokens = 4092
                responseMimeType = "text/plain"
            }
        )
    }

    private fun getChatHistory(): List<Content> {
        return listOf(
            content("user") {
                text("Bedzie starał się naprawiać błedy przy odczytywaniu tesktu OCR. Ignoruj przedrostki np: \"D_\" lub \"MC_\", ignoruj \"luz\". Np jeśli podam \"Chust Delikat x150\" masz odpowiedzieć \"Chusteczki delikatne x150\", bez zadnego komentarza. Po nazwie podaj też przypisanie do jednej z kategorii: [INNE,JEDZENIE,ZDROWIE,KULTURA,OPŁATY,KOSTMETYKI,SPORT,MOTORYZACJA,UBRANIA,PALIWO]. Odpowiedź utrzymaj w formacie: \"<nazwa> | <kategoria>\". Np. \"Chusteczki delikatne x150 | INNE\"")
            }
        )
    }

}
