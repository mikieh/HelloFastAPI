package com.callfinder.app.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.callfinder.app.domain.model.SyncFrequency
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurePreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val regularPrefs: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    // Gemini API Key (encrypted)
    fun getGeminiApiKey(): String? = securePrefs.getString(KEY_GEMINI_API_KEY, null)

    fun setGeminiApiKey(apiKey: String) {
        securePrefs.edit().putString(KEY_GEMINI_API_KEY, apiKey).apply()
    }

    fun clearGeminiApiKey() {
        securePrefs.edit().remove(KEY_GEMINI_API_KEY).apply()
    }

    // Scan folder path
    fun getScanFolderPath(): String =
        regularPrefs.getString(KEY_SCAN_FOLDER, DEFAULT_SCAN_FOLDER) ?: DEFAULT_SCAN_FOLDER

    fun setScanFolderPath(path: String) {
        regularPrefs.edit().putString(KEY_SCAN_FOLDER, path).apply()
    }

    // Sync frequency
    fun getSyncFrequency(): SyncFrequency {
        val value = regularPrefs.getString(KEY_SYNC_FREQUENCY, SyncFrequency.WEEKLY.name)
        return try {
            SyncFrequency.valueOf(value ?: SyncFrequency.WEEKLY.name)
        } catch (e: IllegalArgumentException) {
            SyncFrequency.WEEKLY
        }
    }

    fun setSyncFrequency(frequency: SyncFrequency) {
        regularPrefs.edit().putString(KEY_SYNC_FREQUENCY, frequency.name).apply()
    }

    // Sync time (hour and minute)
    fun getSyncHour(): Int = regularPrefs.getInt(KEY_SYNC_HOUR, 22)
    fun getSyncMinute(): Int = regularPrefs.getInt(KEY_SYNC_MINUTE, 0)

    fun setSyncTime(hour: Int, minute: Int) {
        regularPrefs.edit()
            .putInt(KEY_SYNC_HOUR, hour)
            .putInt(KEY_SYNC_MINUTE, minute)
            .apply()
    }

    // Last sync time
    fun getLastSyncTime(): Long? {
        val time = regularPrefs.getLong(KEY_LAST_SYNC, -1L)
        return if (time == -1L) null else time
    }

    fun setLastSyncTime(time: Long) {
        regularPrefs.edit().putLong(KEY_LAST_SYNC, time).apply()
    }

    // Min transcription duration
    fun getMinTranscriptionDuration(): Int =
        regularPrefs.getInt(KEY_MIN_TRANSCRIPTION_DURATION, 30)

    fun setMinTranscriptionDuration(seconds: Int) {
        regularPrefs.edit().putInt(KEY_MIN_TRANSCRIPTION_DURATION, seconds).apply()
    }

    // Wi-Fi only transcription
    fun isWifiOnlyTranscription(): Boolean =
        regularPrefs.getBoolean(KEY_WIFI_ONLY, true)

    fun setWifiOnlyTranscription(wifiOnly: Boolean) {
        regularPrefs.edit().putBoolean(KEY_WIFI_ONLY, wifiOnly).apply()
    }

    // Language preference
    fun getLanguagePreference(): String =
        regularPrefs.getString(KEY_LANGUAGE, "auto") ?: "auto"

    fun setLanguagePreference(language: String) {
        regularPrefs.edit().putString(KEY_LANGUAGE, language).apply()
    }

    companion object {
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"
        private const val KEY_SCAN_FOLDER = "scan_folder"
        private const val KEY_SYNC_FREQUENCY = "sync_frequency"
        private const val KEY_SYNC_HOUR = "sync_hour"
        private const val KEY_SYNC_MINUTE = "sync_minute"
        private const val KEY_LAST_SYNC = "last_sync"
        private const val KEY_MIN_TRANSCRIPTION_DURATION = "min_transcription_duration"
        private const val KEY_WIFI_ONLY = "wifi_only"
        private const val KEY_LANGUAGE = "language"
        const val DEFAULT_SCAN_FOLDER = "recordings/calls"
    }
}
