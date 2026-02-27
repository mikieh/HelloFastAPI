package com.callarchive.app.domain.model

data class CallRecording(
    val id: String,
    val filePath: String,
    val fileName: String,
    val origin: String?,
    val destination: String?,
    val contactName: String?,
    val callType: CallType,
    val durationSeconds: Int,
    val recordedAt: Long,
    val fileSizeBytes: Long,
    val driveFileId: String?,
    val uploadedAt: Long?,
    val transcribed: Boolean,
    val transcript: String?,
    val summary: String?,
    val keywords: List<String>
) {
    val isUploaded: Boolean get() = driveFileId != null
    val isPendingTranscription: Boolean get() = !transcribed && durationSeconds > 30
}
