package com.callfinder.app.domain.repository

import com.callfinder.app.domain.model.TranscriptionResult
import java.io.File

interface TranscriptionRepository {
    suspend fun transcribe(audioFile: File): TranscriptionResult
    suspend fun validateApiKey(apiKey: String): Boolean
}
