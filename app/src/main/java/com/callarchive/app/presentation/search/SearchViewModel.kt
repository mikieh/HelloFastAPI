package com.callarchive.app.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callarchive.app.domain.model.CallRecording
import com.callarchive.app.domain.model.CallType
import com.callarchive.app.domain.model.SearchQuery
import com.callarchive.app.domain.repository.CallRecordingRepository
import com.callarchive.app.domain.usecase.SearchRecordingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val phoneNumber: String = "",
    val contactName: String = "",
    val dateFrom: Long? = null,
    val dateTo: Long? = null,
    val minDurationSeconds: String = "",
    val callType: CallType? = null,
    val selectedKeywords: List<String> = emptyList(),
    val textQuery: String = "",
    val availableKeywords: List<String> = emptyList(),
    val results: List<CallRecording> = emptyList(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
    val showFilters: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchUseCase: SearchRecordingsUseCase,
    private val repository: CallRecordingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadKeywords()
    }

    private fun loadKeywords() {
        viewModelScope.launch {
            repository.getAllKeywords().collect { keywords ->
                _uiState.update { it.copy(availableKeywords = keywords) }
            }
        }
    }

    fun updateTextQuery(query: String) {
        _uiState.update { it.copy(textQuery = query) }
    }

    fun updatePhoneNumber(number: String) {
        _uiState.update { it.copy(phoneNumber = number) }
    }

    fun updateContactName(name: String) {
        _uiState.update { it.copy(contactName = name) }
    }

    fun updateDateRange(from: Long?, to: Long?) {
        _uiState.update { it.copy(dateFrom = from, dateTo = to) }
    }

    fun updateMinDuration(duration: String) {
        _uiState.update { it.copy(minDurationSeconds = duration) }
    }

    fun updateCallType(type: CallType?) {
        _uiState.update { it.copy(callType = type) }
    }

    fun toggleKeyword(keyword: String) {
        _uiState.update { state ->
            val keywords = if (keyword in state.selectedKeywords) {
                state.selectedKeywords - keyword
            } else {
                state.selectedKeywords + keyword
            }
            state.copy(selectedKeywords = keywords)
        }
    }

    fun toggleFilters() {
        _uiState.update { it.copy(showFilters = !it.showFilters) }
    }

    fun search() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, hasSearched = true) }

            val state = _uiState.value
            val query = SearchQuery(
                phoneNumber = state.phoneNumber.takeIf { it.isNotBlank() },
                contactName = state.contactName.takeIf { it.isNotBlank() },
                dateFrom = state.dateFrom,
                dateTo = state.dateTo,
                minDurationSeconds = state.minDurationSeconds.toIntOrNull(),
                callType = state.callType,
                keywords = state.selectedKeywords,
                textQuery = state.textQuery.takeIf { it.isNotBlank() }
            )

            searchUseCase(query).collect { results ->
                _uiState.update {
                    it.copy(results = results, isSearching = false)
                }
            }
        }
    }

    fun clearFilters() {
        _uiState.update {
            SearchUiState(availableKeywords = it.availableKeywords)
        }
    }
}
