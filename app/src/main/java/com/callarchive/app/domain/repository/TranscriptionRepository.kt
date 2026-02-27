package com.callarchive.app.domain.repository

import com.callarchive.app.domain.model.TranscriptionResult
import java.io.File

interface TranscriptionRepository {
    suspend fun transcribe(audioFile: File): TranscriptionResult
    suspend fun validateApiKey(apiKey: String): Boolean
}
