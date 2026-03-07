package com.callfinder.app.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callfinder.app.data.local.preferences.SecurePreferencesManager
import com.callfinder.app.data.remote.drive.DriveService
import com.callfinder.app.domain.model.SyncFrequency
import com.callfinder.app.domain.repository.TranscriptionRepository
import com.callfinder.app.worker.SyncScheduler
import com.google.android.gms.auth.api.signin.GoogleSignIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isSignedIn: Boolean = false,
    val googleAccountEmail: String? = null,
    val syncFrequency: SyncFrequency = SyncFrequency.WEEKLY,
    val syncHour: Int = 22,
    val syncMinute: Int = 0,
    val scanFolderPath: String = SecurePreferencesManager.DEFAULT_SCAN_FOLDER,
    val geminiApiKey: String = "",
    val isApiKeyVisible: Boolean = false,
    val isValidatingApiKey: Boolean = false,
    val apiKeyValidationResult: Boolean? = null,
    val minTranscriptionDuration: Int = 30,
    val wifiOnly: Boolean = true,
    val language: String = "auto",
    val lastSyncTime: Long? = null,
    val nextSyncTime: Long? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: SecurePreferencesManager,
    private val transcriptionRepository: TranscriptionRepository,
    private val driveService: DriveService,
    private val syncScheduler: SyncScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        _uiState.update {
            it.copy(
                isSignedIn = account != null,
                googleAccountEmail = account?.email,
                syncFrequency = preferencesManager.getSyncFrequency(),
                syncHour = preferencesManager.getSyncHour(),
                syncMinute = preferencesManager.getSyncMinute(),
                scanFolderPath = preferencesManager.getScanFolderPath(),
                geminiApiKey = preferencesManager.getGeminiApiKey() ?: "",
                minTranscriptionDuration = preferencesManager.getMinTranscriptionDuration(),
                wifiOnly = preferencesManager.isWifiOnlyTranscription(),
                language = preferencesManager.getLanguagePreference(),
                lastSyncTime = preferencesManager.getLastSyncTime()
            )
        }
    }

    fun updateSyncFrequency(frequency: SyncFrequency) {
        preferencesManager.setSyncFrequency(frequency)
        syncScheduler.schedulePeriodic(frequency, preferencesManager.getSyncHour(), preferencesManager.getSyncMinute())
        _uiState.update { it.copy(syncFrequency = frequency) }
    }

    fun updateSyncTime(hour: Int, minute: Int) {
        preferencesManager.setSyncTime(hour, minute)
        syncScheduler.schedulePeriodic(preferencesManager.getSyncFrequency(), hour, minute)
        _uiState.update { it.copy(syncHour = hour, syncMinute = minute) }
    }

    fun updateScanFolderPath(path: String) {
        preferencesManager.setScanFolderPath(path)
        _uiState.update { it.copy(scanFolderPath = path) }
    }

    fun updateGeminiApiKey(key: String) {
        _uiState.update { it.copy(geminiApiKey = key, apiKeyValidationResult = null) }
    }

    fun toggleApiKeyVisibility() {
        _uiState.update { it.copy(isApiKeyVisible = !it.isApiKeyVisible) }
    }

    fun saveAndValidateApiKey() {
        val key = _uiState.value.geminiApiKey.trim()
        if (key.isBlank()) {
            preferencesManager.clearGeminiApiKey()
            _uiState.update { it.copy(apiKeyValidationResult = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isValidatingApiKey = true) }
            val isValid = transcriptionRepository.validateApiKey(key)
            if (isValid) {
                preferencesManager.setGeminiApiKey(key)
            }
            _uiState.update {
                it.copy(
                    isValidatingApiKey = false,
                    apiKeyValidationResult = isValid
                )
            }
        }
    }

    fun updateMinTranscriptionDuration(seconds: Int) {
        preferencesManager.setMinTranscriptionDuration(seconds)
        _uiState.update { it.copy(minTranscriptionDuration = seconds) }
    }

    fun updateWifiOnly(wifiOnly: Boolean) {
        preferencesManager.setWifiOnlyTranscription(wifiOnly)
        _uiState.update { it.copy(wifiOnly = wifiOnly) }
    }

    fun updateLanguage(language: String) {
        preferencesManager.setLanguagePreference(language)
        _uiState.update { it.copy(language = language) }
    }

    fun onGoogleSignInResult(success: Boolean, email: String?) {
        _uiState.update { it.copy(isSignedIn = success, googleAccountEmail = email) }
    }

    fun signOut() {
        driveService.signOut()
        _uiState.update { it.copy(isSignedIn = false, googleAccountEmail = null) }
    }
}
