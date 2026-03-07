package com.callfinder.app.presentation.recordings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callfinder.app.domain.model.CallRecording
import com.callfinder.app.domain.repository.CallRecordingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOption {
    DATE_DESC, DATE_ASC, DURATION_DESC, DURATION_ASC, NAME_ASC
}

enum class FilterOption {
    ALL, UPLOADED, NOT_UPLOADED, TRANSCRIBED, NOT_TRANSCRIBED
}

data class RecordingsUiState(
    val recordings: List<CallRecording> = emptyList(),
    val filteredRecordings: List<CallRecording> = emptyList(),
    val isLoading: Boolean = true,
    val sortOption: SortOption = SortOption.DATE_DESC,
    val filterOption: FilterOption = FilterOption.ALL,
    val error: String? = null
)

@HiltViewModel
class RecordingsViewModel @Inject constructor(
    private val repository: CallRecordingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordingsUiState())
    val uiState: StateFlow<RecordingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllRecordings().collect { recordings ->
                _uiState.update {
                    it.copy(
                        recordings = recordings,
                        isLoading = false
                    )
                }
                applyFiltersAndSort()
            }
        }
    }

    fun setSortOption(option: SortOption) {
        _uiState.update { it.copy(sortOption = option) }
        applyFiltersAndSort()
    }

    fun setFilterOption(option: FilterOption) {
        _uiState.update { it.copy(filterOption = option) }
        applyFiltersAndSort()
    }

    private fun applyFiltersAndSort() {
        val state = _uiState.value
        var result = state.recordings

        // Apply filter
        result = when (state.filterOption) {
            FilterOption.ALL -> result
            FilterOption.UPLOADED -> result.filter { it.isUploaded }
            FilterOption.NOT_UPLOADED -> result.filter { !it.isUploaded }
            FilterOption.TRANSCRIBED -> result.filter { it.transcribed }
            FilterOption.NOT_TRANSCRIBED -> result.filter { !it.transcribed }
        }

        // Apply sort
        result = when (state.sortOption) {
            SortOption.DATE_DESC -> result.sortedByDescending { it.recordedAt }
            SortOption.DATE_ASC -> result.sortedBy { it.recordedAt }
            SortOption.DURATION_DESC -> result.sortedByDescending { it.durationSeconds }
            SortOption.DURATION_ASC -> result.sortedBy { it.durationSeconds }
            SortOption.NAME_ASC -> result.sortedBy { it.contactName ?: it.origin ?: "" }
        }

        _uiState.update { it.copy(filteredRecordings = result) }
    }
}
