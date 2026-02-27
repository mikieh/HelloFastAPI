package com.callarchive.app.data.remote.gemini

import com.google.gson.annotations.SerializedName

// Request models
data class GeminiRequest(
    val contents: List<GeminiContent>,
    @SerializedName("generationConfig")
    val generationConfig: GenerationConfig? = null
)

data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String = "user"
)

data class GeminiPart(
    val text: String? = null,
    @SerializedName("inline_data")
    val inlineData: InlineData? = null
)

data class InlineData(
    @SerializedName("mime_type")
    val mimeType: String,
    val data: String // base64 encoded
)

data class GenerationConfig(
    val temperature: Float = 0.1f,
    @SerializedName("responseMimeType")
    val responseMimeType: String = "application/json"
)

// Response models
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

data class GeminiCandidate(
    val content: GeminiResponseContent?
)

data class GeminiResponseContent(
    val parts: List<GeminiResponsePart>?
)

data class GeminiResponsePart(
    val text: String?
)

// Parsed transcription response
data class TranscriptionResponse(
    val transcript: String,
    val keywords: List<String>,
    val summary: String
)
