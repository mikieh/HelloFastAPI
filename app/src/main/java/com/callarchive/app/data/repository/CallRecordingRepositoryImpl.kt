package com.callarchive.app.data.repository

import com.callarchive.app.data.local.db.CallRecordingDao
import com.callarchive.app.data.local.db.CallRecordingEntity
import com.callarchive.app.domain.model.CallRecording
import com.callarchive.app.domain.model.SearchQuery
import com.callarchive.app.domain.repository.CallRecordingRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRecordingRepositoryImpl @Inject constructor(
    private val dao: CallRecordingDao,
    private val gson: Gson
) : CallRecordingRepository {

    override fun getAllRecordings(): Flow<List<CallRecording>> {
        return dao.getAllRecordings().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRecordingById(id: String): Flow<CallRecording?> {
        return dao.getRecordingById(id).map { it?.toDomain() }
    }

    override fun searchRecordings(query: SearchQuery): Flow<List<CallRecording>> {
        return dao.searchRecordings(
            phoneNumber = query.phoneNumber,
            contactName = query.contactName,
            dateFrom = query.dateFrom,
            dateTo = query.dateTo,
            minDuration = query.minDurationSeconds,
            callType = query.callType?.name,
            textQuery = query.textQuery
        ).map { entities ->
            var results = entities.map { it.toDomain() }

            // Post-filter by keywords if specified (Room can't easily query JSON arrays)
            if (query.keywords.isNotEmpty()) {
                results = results.filter { recording ->
                    query.keywords.any { keyword ->
                        recording.keywords.any { it.contains(keyword, ignoreCase = true) }
                    }
                }
            }

            results
        }
    }

    override fun getAllKeywords(): Flow<List<String>> {
        return dao.getAllKeywordsRaw().map { rawList ->
            val type = object : TypeToken<List<String>>() {}.type
            rawList.flatMap { json ->
                try {
                    gson.fromJson<List<String>>(json, type)
                } catch (e: Exception) {
                    emptyList()
                }
            }.distinct().sorted()
        }
    }

    override suspend fun getRecordingCount(): Int = dao.getRecordingCount()
    override suspend fun getUploadedCount(): Int = dao.getUploadedCount()
    override suspend fun getPendingTranscriptionCount(): Int = dao.getPendingTranscriptionCount()

    override suspend fun insertRecording(recording: CallRecording) {
        dao.insert(CallRecordingEntity.fromDomain(recording))
    }

    override suspend fun insertRecordings(recordings: List<CallRecording>) {
        dao.insertAll(recordings.map { CallRecordingEntity.fromDomain(it) })
    }

    override suspend fun updateRecording(recording: CallRecording) {
        dao.update(CallRecordingEntity.fromDomain(recording))
    }

    override suspend fun deleteRecording(id: String) {
        dao.delete(id)
    }

    override suspend fun getUnuploadedRecordings(): List<CallRecording> {
        return dao.getUnuploadedRecordings().map { it.toDomain() }
    }

    override suspend fun getUntranscribedRecordings(minDurationSeconds: Int): List<CallRecording> {
        return dao.getUntranscribedRecordings(minDurationSeconds).map { it.toDomain() }
    }

    override suspend fun recordingExists(id: String): Boolean {
        return dao.recordingExists(id)
    }
}
