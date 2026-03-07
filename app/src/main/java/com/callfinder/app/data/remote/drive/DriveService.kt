package com.callfinder.app.data.remote.drive

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var driveService: Drive? = null
    private var archiveFolderId: String? = null

    fun isAuthenticated(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }

    private fun getDriveService(): Drive {
        driveService?.let { return it }

        val account = GoogleSignIn.getLastSignedInAccount(context)
            ?: throw IllegalStateException("Not signed in to Google")

        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account.account

        val service = Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("CallFinder")
            .build()

        driveService = service
        return service
    }

    suspend fun ensureFolder(folderName: String): String = withContext(Dispatchers.IO) {
        archiveFolderId?.let { return@withContext it }

        val drive = getDriveService()

        // Check if folder already exists
        val result = drive.files().list()
            .setQ("name = '$folderName' and mimeType = 'application/vnd.google-apps.folder' and trashed = false")
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()

        val folderId = if (result.files.isNotEmpty()) {
            result.files[0].id
        } else {
            val folderMetadata = com.google.api.services.drive.model.File().apply {
                name = folderName
                mimeType = "application/vnd.google-apps.folder"
            }
            drive.files().create(folderMetadata)
                .setFields("id")
                .execute()
                .id
        }

        archiveFolderId = folderId
        folderId
    }

    suspend fun uploadFile(
        file: File,
        fileName: String,
        folderId: String,
        onProgress: (Float) -> Unit = {}
    ): String = withContext(Dispatchers.IO) {
        val drive = getDriveService()

        val fileMetadata = com.google.api.services.drive.model.File().apply {
            name = fileName
            parents = listOf(folderId)
        }

        val mimeType = getMimeType(file.extension)
        val mediaContent = FileContent(mimeType, file)

        onProgress(0f)
        val uploadedFile = drive.files().create(fileMetadata, mediaContent)
            .setFields("id")
            .execute()
        onProgress(1f)

        uploadedFile.id
    }

    suspend fun deleteFile(driveFileId: String) = withContext(Dispatchers.IO) {
        val drive = getDriveService()
        drive.files().delete(driveFileId).execute()
    }

    fun signOut() {
        driveService = null
        archiveFolderId = null
    }

    private fun getMimeType(extension: String): String {
        return when (extension.lowercase()) {
            "mp3" -> "audio/mpeg"
            "m4a" -> "audio/mp4"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            "3gp" -> "audio/3gpp"
            "amr" -> "audio/amr"
            else -> "application/octet-stream"
        }
    }
}
