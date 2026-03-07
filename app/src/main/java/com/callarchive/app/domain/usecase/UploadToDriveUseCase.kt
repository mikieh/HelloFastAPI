package com.callarchive.app.domain.usecase

import com.callarchive.app.domain.model.CallRecording
import com.callarchive.app.domain.repository.CallRecordingRepository
import com.callarchive.app.domain.repository.DriveRepository
import java.io.File
import javax.inject.Inject

class UploadToDriveUseCase @Inject constructor(
    private val driveRepository: DriveRepository,
    private val recordingRepository: CallRecordingRepository
) {
    suspend operator fun invoke(
        recording: CallRecording,
        onProgress: (Float) -> Unit = {}
    ): Result<CallRecording> {
        return try {
            driveRepository.ensureFolder("CallFinder")
            val driveFileId = driveRepository.uploadFile(
                file = File(recording.filePath),
                fileName = recording.fileName,
                onProgress = onProgress
            )
            val updated = recording.copy(
                driveFileId = driveFileId,
                uploadedAt = System.currentTimeMillis()
            )
            recordingRepository.updateRecording(updated)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
