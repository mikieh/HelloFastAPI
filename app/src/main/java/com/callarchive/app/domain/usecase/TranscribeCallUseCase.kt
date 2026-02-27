package com.callarchive.app.domain.usecase

import com.callarchive.app.domain.model.CallRecording
import com.callarchive.app.domain.repository.CallRecordingRepository
import com.callarchive.app.domain.repository.TranscriptionRepository
import java.io.File
import javax.inject.Inject

class TranscribeCallUseCase @Inject constructor(
    private val transcriptionRepository: TranscriptionRepository,
    private val recordingRepository: CallRecordingRepository
) {
    suspend operator fun invoke(recording: CallRecording): Result<CallRecording> {
        return try {
            val result = transcriptionRepository.transcribe(File(recording.filePath))
            val updated = recording.copy(
                transcribed = true,
                transcript = result.transcript,
                summary = result.summary,
                keywords = result.keywords
            )
            recordingRepository.updateRecording(updated)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
