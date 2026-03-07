package com.callfinder.app.data.repository

import com.callfinder.app.data.remote.gemini.GeminiService
import com.callfinder.app.domain.model.TranscriptionResult
import com.callfinder.app.domain.repository.TranscriptionRepository
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionRepositoryImpl @Inject constructor(
    private val geminiService: GeminiService
) : TranscriptionRepository {

    override suspend fun transcribe(audioFile: File): TranscriptionResult {
        return geminiService.transcribe(audioFile)
    }

    override suspend fun validateApiKey(apiKey: String): Boolean {
        return geminiService.validateApiKey(apiKey)
    }
}
