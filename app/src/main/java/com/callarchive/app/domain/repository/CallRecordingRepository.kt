package com.callarchive.app.domain.repository

import com.callarchive.app.domain.model.CallRecording
import com.callarchive.app.domain.model.SearchQuery
import kotlinx.coroutines.flow.Flow

interface CallRecordingRepository {
    fun getAllRecordings(): Flow<List<CallRecording>>
    fun getRecordingById(id: String): Flow<CallRecording?>
    fun searchRecordings(query: SearchQuery): Flow<List<CallRecording>>
    fun getAllKeywords(): Flow<List<String>>
    suspend fun getRecordingCount(): Int
    suspend fun getUploadedCount(): Int
    suspend fun getPendingTranscriptionCount(): Int
    suspend fun insertRecording(recording: CallRecording)
    suspend fun insertRecordings(recordings: List<CallRecording>)
    suspend fun updateRecording(recording: CallRecording)
    suspend fun deleteRecording(id: String)
    suspend fun getUnuploadedRecordings(): List<CallRecording>
    suspend fun getUntranscribedRecordings(minDurationSeconds: Int): List<CallRecording>
    suspend fun recordingExists(id: String): Boolean
}
