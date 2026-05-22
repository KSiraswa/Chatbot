package com.example.chatbot.ai

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GeminiClient(apiKey: String) {

    private val model = GenerativeModel(
        modelName = "gemini-3.5-flash",
        apiKey = apiKey,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 1024
        }
    )

    suspend fun generateResponse(prompt: String, image: Bitmap? = null): String {
        android.util.Log.d("GeminiClient", "Sending prompt: $prompt")
        val input = content {
            if (image != null) {
                image(image)
            }
            text(prompt)
        }
        return try {
            val response = model.generateContent(input)
            response.text ?: "I couldn't understand that."
        } catch (e: Exception) {
            e.printStackTrace()
            "Error: ${e.localizedMessage ?: "Unknown error occurred"}. Please check your API key and network connection."
        }
    }
}
