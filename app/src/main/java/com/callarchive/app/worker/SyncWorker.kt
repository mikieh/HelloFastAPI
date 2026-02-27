package com.callarchive.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.callarchive.app.R
import com.callarchive.app.data.local.preferences.SecurePreferencesManager
import com.callarchive.app.domain.usecase.SyncAllUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncAllUseCase: SyncAllUseCase,
    private val preferencesManager: SecurePreferencesManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        setForeground(createForegroundInfo("Starting sync…"))

        val scanPath = preferencesManager.getScanFolderPath()

        return syncAllUseCase(scanPath) { current, total, step ->
            setForeground(createForegroundInfo(step))
        }.fold(
            onSuccess = {
                preferencesManager.setLastSyncTime(System.currentTimeMillis())
                Result.success()
            },
            onFailure = { e ->
                if (runAttemptCount < 3) Result.retry() else Result.failure()
            }
        )
    }

    private fun createForegroundInfo(progress: String): ForegroundInfo {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("CallArchive Sync")
            .setContentText(progress)
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setOngoing(true)
            .build()

        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sync",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Call recording sync notifications"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val WORK_NAME = "call_archive_sync"
        const val CHANNEL_ID = "sync_channel"
        const val NOTIFICATION_ID = 1001
    }
}
