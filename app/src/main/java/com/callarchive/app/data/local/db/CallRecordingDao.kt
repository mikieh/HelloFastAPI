package com.callarchive.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CallRecordingDao {

    @Query("SELECT * FROM call_recordings ORDER BY recordedAt DESC")
    fun getAllRecordings(): Flow<List<CallRecordingEntity>>

    @Query("SELECT * FROM call_recordings WHERE id = :id")
    fun getRecordingById(id: String): Flow<CallRecordingEntity?>

    @Query("SELECT COUNT(*) FROM call_recordings")
    suspend fun getRecordingCount(): Int

    @Query("SELECT COUNT(*) FROM call_recordings WHERE driveFileId IS NOT NULL")
    suspend fun getUploadedCount(): Int

    @Query("SELECT COUNT(*) FROM call_recordings WHERE transcribed = 0 AND durationSeconds > 30")
    suspend fun getPendingTranscriptionCount(): Int

    @Query("SELECT * FROM call_recordings WHERE driveFileId IS NULL")
    suspend fun getUnuploadedRecordings(): List<CallRecordingEntity>

    @Query("SELECT * FROM call_recordings WHERE transcribed = 0 AND durationSeconds > :minDuration")
    suspend fun getUntranscribedRecordings(minDuration: Int): List<CallRecordingEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM call_recordings WHERE id = :id)")
    suspend fun recordingExists(id: String): Boolean

    @Query("""
        SELECT * FROM call_recordings
        WHERE (:phoneNumber IS NULL OR origin LIKE '%' || :phoneNumber || '%' OR destination LIKE '%' || :phoneNumber || '%')
        AND (:contactName IS NULL OR contactName LIKE '%' || :contactName || '%')
        AND (:dateFrom IS NULL OR recordedAt >= :dateFrom)
        AND (:dateTo IS NULL OR recordedAt <= :dateTo)
        AND (:minDuration IS NULL OR durationSeconds >= :minDuration)
        AND (:callType IS NULL OR callType = :callType)
        AND (:textQuery IS NULL OR transcript LIKE '%' || :textQuery || '%'
             OR contactName LIKE '%' || :textQuery || '%'
             OR origin LIKE '%' || :textQuery || '%'
             OR destination LIKE '%' || :textQuery || '%'
             OR summary LIKE '%' || :textQuery || '%'
             OR keywords LIKE '%' || :textQuery || '%')
        ORDER BY recordedAt DESC
    """)
    fun searchRecordings(
        phoneNumber: String?,
        contactName: String?,
        dateFrom: Long?,
        dateTo: Long?,
        minDuration: Int?,
        callType: String?,
        textQuery: String?
    ): Flow<List<CallRecordingEntity>>

    @Query("SELECT DISTINCT keywords FROM call_recordings WHERE keywords != '[]'")
    fun getAllKeywordsRaw(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recording: CallRecordingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recordings: List<CallRecordingEntity>)

    @Update
    suspend fun update(recording: CallRecordingEntity)

    @Query("DELETE FROM call_recordings WHERE id = :id")
    suspend fun delete(id: String)
}
