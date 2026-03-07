package com.callfinder.app.domain.model

data class SyncStatus(
    val lastSyncTime: Long?,
    val nextSyncTime: Long?,
    val isRunning: Boolean,
    val lastError: String?
)
