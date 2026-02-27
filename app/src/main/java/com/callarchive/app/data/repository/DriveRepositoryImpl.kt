package com.callarchive.app.data.repository

import com.callarchive.app.data.remote.drive.DriveService
import com.callarchive.app.domain.repository.DriveRepository
import kotlinx.coroutines.delay
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveRepositoryImpl @Inject constructor(
    private val driveService: DriveService
) : DriveRepository {

    private var archiveFolderId: String? = null

    override suspend fun isAuthenticated(): Boolean {
        return driveService.isAuthenticated()
    }

    override suspend fun uploadFile(
        file: File,
        fileName: String,
        onProgress: (Float) -> Unit
    ): String {
        val folderId = ensureFolder("CallArchive")
        return retryWithBackoff {
            driveService.uploadFile(file, fileName, folderId, onProgress)
        }
    }

    override suspend fun ensureFolder(folderName: String): String {
        archiveFolderId?.let { return it }
        val id = driveService.ensureFolder(folderName)
        archiveFolderId = id
        return id
    }

    override suspend fun deleteFile(driveFileId: String) {
        driveService.deleteFile(driveFileId)
    }

    private suspend fun <T> retryWithBackoff(
        maxRetries: Int = 3,
        initialDelayMs: Long = 1000,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMs
        repeat(maxRetries - 1) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                delay(currentDelay)
                currentDelay *= 2
            }
        }
        return block() // Last attempt, let exception propagate
    }
}
