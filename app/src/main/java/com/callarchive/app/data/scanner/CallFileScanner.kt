package com.callarchive.app.data.scanner

import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.provider.CallLog
import com.callarchive.app.domain.model.CallRecording
import com.callarchive.app.domain.model.CallType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallFileScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val supportedExtensions = setOf("mp3", "m4a", "wav", "ogg", "3gp", "amr")

    // Patterns: +972501234567_20240101_120000.ext or 0501234567_2024-01-01_12-00-00.ext
    private val filenamePattern = Regex(
        """^(\+?\d{7,15})?[_\-]?(\d{4}[\-_]?\d{2}[\-_]?\d{2})[_\-]?(\d{2}[\-_]?\d{2}[\-_]?\d{2}).*\.(\w+)$"""
    )

    suspend fun scanDirectory(relativePath: String): List<CallRecording> {
        val baseDir = Environment.getExternalStorageDirectory()
        val scanDir = File(baseDir, relativePath)

        if (!scanDir.exists() || !scanDir.isDirectory) {
            return emptyList()
        }

        return scanDir.listFiles()
            ?.filter { it.isFile && it.extension.lowercase() in supportedExtensions }
            ?.mapNotNull { file -> parseRecording(file) }
            ?: emptyList()
    }

    private fun parseRecording(file: File): CallRecording? {
        val id = generateId(file.absolutePath)
        val fileName = file.name
        val fileSizeBytes = file.length()

        // Extract duration from audio metadata
        val durationSeconds = extractDuration(file)

        // Parse phone number and date from filename
        val match = filenamePattern.matchEntire(fileName)
        val phoneNumber = match?.groupValues?.get(1)?.takeIf { it.isNotEmpty() }
        val dateStr = match?.groupValues?.get(2)?.replace("-", "")?.replace("_", "")
        val timeStr = match?.groupValues?.get(3)?.replace("-", "")?.replace("_", "")

        val recordedAt = parseDateTime(dateStr, timeStr) ?: file.lastModified()

        // Try to enrich from CallLog
        val callLogInfo = queryCallLog(phoneNumber, recordedAt)

        return CallRecording(
            id = id,
            filePath = file.absolutePath,
            fileName = fileName,
            origin = callLogInfo?.origin ?: phoneNumber,
            destination = callLogInfo?.destination,
            contactName = callLogInfo?.contactName,
            callType = callLogInfo?.callType ?: CallType.UNKNOWN,
            durationSeconds = durationSeconds,
            recordedAt = recordedAt,
            fileSizeBytes = fileSizeBytes,
            driveFileId = null,
            uploadedAt = null,
            transcribed = false,
            transcript = null,
            summary = null,
            keywords = emptyList()
        )
    }

    private fun extractDuration(file: File): Int {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val durationMs = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )?.toLongOrNull() ?: 0L
            retriever.release()
            (durationMs / 1000).toInt()
        } catch (e: Exception) {
            0
        }
    }

    private fun parseDateTime(dateStr: String?, timeStr: String?): Long? {
        if (dateStr == null || timeStr == null) return null
        return try {
            val format = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
            format.parse("$dateStr$timeStr")?.time
        } catch (e: Exception) {
            null
        }
    }

    private fun queryCallLog(phoneNumber: String?, recordedAt: Long): CallLogInfo? {
        if (phoneNumber == null) return null

        return try {
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.CACHED_NAME,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DATE
                ),
                "${CallLog.Calls.NUMBER} LIKE ? AND ABS(${CallLog.Calls.DATE} - ?) < 60000",
                arrayOf("%$phoneNumber%", recordedAt.toString()),
                "${CallLog.Calls.DATE} DESC"
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val number = it.getString(0)
                    val name = it.getString(1)
                    val type = it.getInt(2)
                    CallLogInfo(
                        origin = if (type == CallLog.Calls.INCOMING_TYPE) number else null,
                        destination = if (type == CallLog.Calls.OUTGOING_TYPE) number else null,
                        contactName = name,
                        callType = when (type) {
                            CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING
                            CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
                            else -> CallType.UNKNOWN
                        }
                    )
                } else null
            }
        } catch (e: SecurityException) {
            null
        }
    }

    private fun generateId(filePath: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(filePath.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    private data class CallLogInfo(
        val origin: String?,
        val destination: String?,
        val contactName: String?,
        val callType: CallType
    )
}
