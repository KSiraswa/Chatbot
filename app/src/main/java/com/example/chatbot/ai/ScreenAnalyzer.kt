package com.example.chatbot.ai

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.example.chatbot.service.ChatbotAccessibilityService
import kotlinx.coroutines.tasks.await

class ScreenAnalyzer(private val context: Context) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun analyzeScreen(screenshot: Bitmap?): String {
        val accessibilityText = ChatbotAccessibilityService.instance?.extractScreenText() ?: ""
        val ocrText = if (screenshot != null) {
            val image = InputImage.fromBitmap(screenshot, 0)
            try {
                val result = recognizer.process(image).await()
                result.text
            } catch (e: Exception) {
                ""
            }
        } else {
            ""
        }

        return buildPrompt(accessibilityText, ocrText)
    }

    private fun buildPrompt(accessibilityText: String, ocrText: String): String {
        val context = if (accessibilityText.isBlank() && ocrText.isBlank()) {
            "No specific content detected on screen."
        } else {
            "--- Accessibility Text ---\n$accessibilityText\n\n--- OCR Text ---\n$ocrText"
        }

        return """
            $context
            
            Based on the above screen content (if any), explain what is visible and answer the user's request.
        """.trimIndent()
    }
}
