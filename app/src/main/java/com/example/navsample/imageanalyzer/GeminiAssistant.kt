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
        try {
            val response = chat.sendMessage(prompt).text
            Log.i("Gemini", response ?: "EMPTY")
            return response
        } catch (exception: Exception) {
            Log.e("GEMINI", "GEMINI FAILED", exception)
        }
        return ""
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
    Będziesz naprawiać literówki przy odczytywaniu tekstu OCR. 
    W pierwszej linii otrzymasz kategorie, w kolejnych liniach dostaniesz kolejne produkty. 
    W OCR litery U, V, W są często mylone ze sobą. Ignoruj przedrostki np: "D_" lub "MC_". 
    Np jeśli podam "Chust Delikat x150" masz odpowiedzieć "Chusteczki delikatne x150", 
    bez żadnego komentarza. Jeśli nazwa jest napisana dużymi literami to zmień na małe,
    rozpoczynając od dużej litery. Jeśli w nazwie występuje "luz" to nie tłumacz na "luzem" 
    tylko pozostaw oryginalne. Po nazwie podaj też przypisanie do jednej z kategorii. 
    Odpowiedź utrzymaj w formacie: g", "<nazwa> | <kategoria>". Np. "Chusteczki delikatne x150 | INNE". 
    Jeśli inputem będzie "-" to przepisz go, nie poddawaj analizie. Przykłady tłumaczeń:
    "Poleduica Sop 140g" przetłumacz na "Polędwica sopocka 140g",
    "DOUE SZA MEN THICK" przetłumacz na "Dove szampon men thick"
    "D_MC CYTRYNY LUZ" przetłumacz na "Cytryny luz"
    "Masto Ekstra 200g" przetłumacz na "Masło Ekstra 200g"
    "Ponid gał luz" przetłumacz na "Pomidor gałązka luz"
    "Cebula zotta Luz" przetłumacz na "Cebula złota luz"
    "Ogorek szkl luz" przetłumacz na "Ogórek szklarniowy luz".
            """.trimIndent()
                )
            }
        )
    }
}
