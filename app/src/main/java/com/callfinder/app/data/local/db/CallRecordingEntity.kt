package com.callfinder.app.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.callfinder.app.domain.model.CallRecording
import com.callfinder.app.domain.model.CallType

@Entity(tableName = "call_recordings")
data class CallRecordingEntity(
    @PrimaryKey val id: String,
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
    fun toDomain(): CallRecording = CallRecording(
        id = id,
        filePath = filePath,
        fileName = fileName,
        origin = origin,
        destination = destination,
        contactName = contactName,
        callType = callType,
        durationSeconds = durationSeconds,
        recordedAt = recordedAt,
        fileSizeBytes = fileSizeBytes,
        driveFileId = driveFileId,
        uploadedAt = uploadedAt,
        transcribed = transcribed,
        transcript = transcript,
        summary = summary,
        keywords = keywords
    )

    companion object {
        fun fromDomain(recording: CallRecording): CallRecordingEntity = CallRecordingEntity(
            id = recording.id,
            filePath = recording.filePath,
            fileName = recording.fileName,
            origin = recording.origin,
            destination = recording.destination,
            contactName = recording.contactName,
            callType = recording.callType,
            durationSeconds = recording.durationSeconds,
            recordedAt = recording.recordedAt,
            fileSizeBytes = recording.fileSizeBytes,
            driveFileId = recording.driveFileId,
            uploadedAt = recording.uploadedAt,
            transcribed = recording.transcribed,
            transcript = recording.transcript,
            summary = recording.summary,
            keywords = recording.keywords
        )
    }
}
