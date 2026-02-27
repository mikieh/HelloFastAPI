package com.callarchive.app.domain.repository

import java.io.File

interface DriveRepository {
    suspend fun isAuthenticated(): Boolean
    suspend fun uploadFile(file: File, fileName: String, onProgress: (Float) -> Unit = {}): String
    suspend fun ensureFolder(folderName: String): String
    suspend fun deleteFile(driveFileId: String)
}
