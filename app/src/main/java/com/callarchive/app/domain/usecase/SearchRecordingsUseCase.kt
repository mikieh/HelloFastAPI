package com.callarchive.app.domain.usecase

import com.callarchive.app.domain.model.CallRecording
import com.callarchive.app.domain.model.SearchQuery
import com.callarchive.app.domain.repository.CallRecordingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchRecordingsUseCase @Inject constructor(
    private val repository: CallRecordingRepository
) {
    operator fun invoke(query: SearchQuery): Flow<List<CallRecording>> {
        return repository.searchRecordings(query)
    }
}
