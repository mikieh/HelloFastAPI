package com.callarchive.app.domain.usecase

import com.callarchive.app.data.local.preferences.SecurePreferencesManager
import com.callarchive.app.domain.repository.CallRecordingRepository
import com.callarchive.app.domain.repository.DriveRepository
import javax.inject.Inject

class SyncAllUseCase @Inject constructor(
    private val scanRecordingsUseCase: ScanRecordingsUseCase,
    private val uploadToDriveUseCase: UploadToDriveUseCase,
    private val transcribeCallUseCase: TranscribeCallUseCase,
    private val recordingRepository: CallRecordingRepository,
    private val driveRepository: DriveRepository,
    private val preferencesManager: SecurePreferencesManager
) {
    suspend operator fun invoke(
        scanPath: String,
        onProgress: (current: Int, total: Int, step: String) -> Unit = { _, _, _ -> }
    ): Result<SyncResult> {
        return try {
            // Step 1: Scan for new recordings
            onProgress(0, 0, "Scanning for recordings…")
            val newRecordings = scanRecordingsUseCase(scanPath)

            // Step 2: Upload unuploaded recordings to Drive
            val unuploaded = recordingRepository.getUnuploadedRecordings()
            val totalUpload = unuploaded.size
            var uploadedCount = 0

            if (driveRepository.isAuthenticated()) {
                for ((index, recording) in unuploaded.withIndex()) {
                    onProgress(index + 1, totalUpload, "Uploading ${recording.fileName}…")
                    uploadToDriveUseCase(recording)
                    uploadedCount++
                }
            }

            // Step 3: Transcribe untranscribed recordings
            val minDuration = preferencesManager.getMinTranscriptionDuration()
            val apiKey = preferencesManager.getGeminiApiKey()
            var transcribedCount = 0

            if (apiKey != null) {
                val untranscribed = recordingRepository.getUntranscribedRecordings(minDuration)
                val totalTranscribe = untranscribed.size
                for ((index, recording) in untranscribed.withIndex()) {
                    onProgress(index + 1, totalTranscribe, "Transcribing ${recording.fileName}…")
                    transcribeCallUseCase(recording)
                    transcribedCount++
                }
            }

            Result.success(
                SyncResult(
                    scannedCount = newRecordings.size,
                    uploadedCount = uploadedCount,
                    transcribedCount = transcribedCount
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    data class SyncResult(
        val scannedCount: Int,
        val uploadedCount: Int,
        val transcribedCount: Int
    )
}
