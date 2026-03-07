package com.callfinder.app.domain.usecase

import com.callfinder.app.data.scanner.CallFileScanner
import com.callfinder.app.domain.model.CallRecording
import com.callfinder.app.domain.repository.CallRecordingRepository
import javax.inject.Inject

class ScanRecordingsUseCase @Inject constructor(
    private val scanner: CallFileScanner,
    private val repository: CallRecordingRepository
) {
    suspend operator fun invoke(scanPath: String): List<CallRecording> {
        val scannedRecordings = scanner.scanDirectory(scanPath)
        val newRecordings = scannedRecordings.filter { !repository.recordingExists(it.id) }
        if (newRecordings.isNotEmpty()) {
            repository.insertRecordings(newRecordings)
        }
        return newRecordings
    }
}
