package com.callfinder.app.data.remote.gemini

import android.util.Base64
import com.callfinder.app.data.local.preferences.SecurePreferencesManager
import com.callfinder.app.domain.model.TranscriptionResult
import com.google.gson.Gson
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiService @Inject constructor(
    private val api: GeminiApi,
    private val preferencesManager: SecurePreferencesManager,
    private val gson: Gson
) {
    private val systemPrompt = """
        You are analyzing a phone call recording in Hebrew or English (auto-detect).
        Respond ONLY in JSON format:
        {
          "transcript": "...",
          "keywords": ["keyword1", "keyword2", ...],
          "summary": "one sentence summary"
        }
    """.trimIndent()

    suspend fun transcribe(audioFile: File): TranscriptionResult {
        val apiKey = preferencesManager.getGeminiApiKey()
            ?: throw IllegalStateException("Gemini API key not configured")

        val audioBytes = audioFile.readBytes()
        val base64Audio = Base64.encodeToString(audioBytes, Base64.NO_WRAP)
        val mimeType = getMimeType(audioFile.extension)

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = systemPrompt),
                        GeminiPart(
                            inlineData = InlineData(
                                mimeType = mimeType,
                                data = base64Audio
                            )
                        )
                    )
                )
            ),
            generationConfig = GenerationConfig(
                temperature = 0.1f,
                responseMimeType = "application/json"
            )
        )

        val response = api.generateContent(apiKey, request)
        val text = response.candidates?.firstOrNull()
            ?.content?.parts?.firstOrNull()?.text
            ?: throw IllegalStateException("Empty response from Gemini API")

        val transcriptionResponse = gson.fromJson(text, TranscriptionResponse::class.java)
        return TranscriptionResult(
            transcript = transcriptionResponse.transcript,
            keywords = transcriptionResponse.keywords,
            summary = transcriptionResponse.summary
        )
    }

    suspend fun validateApiKey(apiKey: String): Boolean {
        return try {
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = "Hello"))
                    )
                )
            )
            api.generateContent(apiKey, request)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun getMimeType(extension: String): String {
        return when (extension.lowercase()) {
            "mp3" -> "audio/mpeg"
            "m4a" -> "audio/mp4"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            "3gp" -> "audio/3gpp"
            "amr" -> "audio/amr"
            else -> "audio/mpeg"
        }
    }
}
