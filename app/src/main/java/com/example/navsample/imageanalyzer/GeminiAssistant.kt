package com.example.navsample.imageanalyzer

import android.util.Log
import com.example.navsample.BuildConfig
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig

class GeminiAssistant {


    suspend fun sendRequest(prompt: String): String? {
        val model = getModel()
        val chatHistory = getChatHistory()
        val chat = model.startChat(chatHistory)

        return sendMessage(chat, prompt)

    }

    private suspend fun sendMessage(chat: Chat, prompt: String): String? {

        Log.i("Gemini", "Prompt:\n$prompt")
        val response = chat.sendMessage(prompt).text
        Log.i("Gemini", response ?: "EMPTY")
        return response
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
                text(
                    """
                Bedziesz starał się naprawiać błedy przy odczytywaniu tesktu OCR. 
                W pierwszej linii otrzymasz kategorię, w kojelnych liniach dostaniesz kolejne produty.
                Ignoruj przedrostki np: "D_" lub "MC_", ignoruj "luz". 
                Np jeśli podam "Chust Delikat x150" masz odpowiedzieć "Chusteczki 
                delikatne x150", bez zadnego komentarza. 
                Po nazwie podaj też przypisanie do jednej z kategorii. 
                Odpowiedź utrzymaj w formacie: "<nazwa> | <kategoria>". 
                Np. "Chusteczki delikatne x150 | INNE". 
                Jeśli inputem będzie "-" to przepisz go, nie poddawaj analizie.
            """.trimIndent()
                )
            }
        )
    }
}
