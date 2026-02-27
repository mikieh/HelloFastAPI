package com.callarchive.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callarchive.app.data.local.preferences.SecurePreferencesManager
import com.callarchive.app.domain.model.CallRecording
import com.callarchive.app.domain.repository.CallRecordingRepository
import com.callarchive.app.domain.usecase.SyncAllUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val totalRecordings: Int = 0,
    val uploadedCount: Int = 0,
    val pendingTranscriptionCount: Int = 0,
    val lastSyncTime: Long? = null,
    val isSyncing: Boolean = false,
    val syncProgress: String? = null,
    val recentRecordings: List<CallRecording> = emptyList(),
    val hasApiKey: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CallRecordingRepository,
    private val syncAllUseCase: SyncAllUseCase,
    private val preferencesManager: SecurePreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadStats()
        observeRecordings()
    }

    private fun loadStats() {
        viewModelScope.launch {
            try {
                val total = repository.getRecordingCount()
                val uploaded = repository.getUploadedCount()
                val pending = repository.getPendingTranscriptionCount()
                _uiState.update {
                    it.copy(
                        totalRecordings = total,
                        uploadedCount = uploaded,
                        pendingTranscriptionCount = pending,
                        lastSyncTime = preferencesManager.getLastSyncTime(),
                        hasApiKey = preferencesManager.getGeminiApiKey() != null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private fun observeRecordings() {
        viewModelScope.launch {
            repository.getAllRecordings().collect { recordings ->
                _uiState.update {
                    it.copy(recentRecordings = recordings.take(5))
                }
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, error = null) }
            val scanPath = preferencesManager.getScanFolderPath()

            syncAllUseCase(scanPath) { current, total, step ->
                _uiState.update { it.copy(syncProgress = step) }
            }.onSuccess {
                preferencesManager.setLastSyncTime(System.currentTimeMillis())
                loadStats()
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message) }
            }

            _uiState.update { it.copy(isSyncing = false, syncProgress = null) }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
