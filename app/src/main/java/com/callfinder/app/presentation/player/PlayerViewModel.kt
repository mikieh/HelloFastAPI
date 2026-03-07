package com.callfinder.app.presentation.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.callfinder.app.domain.model.CallRecording
import com.callfinder.app.domain.repository.CallRecordingRepository
import com.callfinder.app.domain.usecase.TranscribeCallUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val recording: CallRecording? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0,
    val durationMs: Long = 0,
    val isTranscribing: Boolean = false,
    val editingKeywords: Boolean = false,
    val newKeyword: String = "",
    val error: String? = null
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CallRecordingRepository,
    private val transcribeUseCase: TranscribeCallUseCase
) : ViewModel() {

    private val recordingId: String = savedStateHandle["recordingId"] ?: ""

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    var player: ExoPlayer? = null
        private set

    init {
        loadRecording()
    }

    private fun loadRecording() {
        viewModelScope.launch {
            repository.getRecordingById(recordingId).collect { recording ->
                _uiState.update { it.copy(recording = recording) }
            }
        }
    }

    fun initializePlayer(exoPlayer: ExoPlayer) {
        player = exoPlayer
        val recording = _uiState.value.recording ?: return

        val mediaItem = MediaItem.fromUri(recording.filePath)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    _uiState.update { it.copy(durationMs = exoPlayer.duration) }
                }
            }
        })

        // Position tracking
        viewModelScope.launch {
            while (isActive) {
                player?.let { p ->
                    _uiState.update { it.copy(currentPositionMs = p.currentPosition) }
                }
                delay(200)
            }
        }
    }

    fun togglePlayPause() {
        player?.let { p ->
            if (p.isPlaying) p.pause() else p.play()
        }
    }

    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
    }

    fun transcribe() {
        val recording = _uiState.value.recording ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isTranscribing = true, error = null) }
            transcribeUseCase(recording)
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
            _uiState.update { it.copy(isTranscribing = false) }
        }
    }

    fun updateNewKeyword(keyword: String) {
        _uiState.update { it.copy(newKeyword = keyword) }
    }

    fun addKeyword() {
        val keyword = _uiState.value.newKeyword.trim()
        val recording = _uiState.value.recording ?: return
        if (keyword.isBlank() || keyword in recording.keywords) return

        viewModelScope.launch {
            val updated = recording.copy(keywords = recording.keywords + keyword)
            repository.updateRecording(updated)
            _uiState.update { it.copy(newKeyword = "") }
        }
    }

    fun removeKeyword(keyword: String) {
        val recording = _uiState.value.recording ?: return
        viewModelScope.launch {
            val updated = recording.copy(keywords = recording.keywords - keyword)
            repository.updateRecording(updated)
        }
    }

    fun releasePlayer() {
        player?.release()
        player = null
    }

    override fun onCleared() {
        releasePlayer()
        super.onCleared()
    }
}
