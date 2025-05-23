package com.anand.player.presentation.screen.mainscreen.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anand.core.models.Music
import com.anand.core.utils.Navigator
import com.anand.core.utils.Screens
import com.anand.inventory.domain.repository.InventoryRepository
import com.anand.player.presentation.screen.mainscreen.events.AudioScreenEvent
import com.anand.player.presentation.screen.mainscreen.events.AudioScreenState
import com.anand.player.service.PlaybackClient
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioScreenViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    @ApplicationContext private val context: Context,
    private val playbackClient: PlaybackClient,
    private val navigator: Navigator
) : ViewModel() {
    private val _uiState = MutableStateFlow(AudioScreenState())
    val uiState = _uiState.asStateFlow()
    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> = _playbackPosition

    private val _playbackDuration = MutableStateFlow(0L)
    val playbackDuration: StateFlow<Long> = _playbackDuration

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering


    init {
        viewModelScope.launch {
            val musics = inventoryRepository.getAllLocalMusic(context)
            _uiState.update {
                it.copy(
                    musicList = musics
                )
            }
        }
        viewModelScope.launch {
            playbackClient.isPlayingFlow.collect { isPlaying ->
                _uiState.update { it.copy(isPlaying = isPlaying) }
            }
        }
        viewModelScope.launch {
            playbackClient.currentMediaItemFlow.collectLatest { mediaItem ->
                mediaItem?.let {
                    val currentSong = _uiState.value.musicList.find { song ->
                        song.contentUri.toString() == it.mediaId
                    }
                    _uiState.update { it.copy(currentSong = currentSong) }
                }
            }
        }

        // Collect playback position updates
        viewModelScope.launch {
            playbackClient.playbackPositionFlow.collectLatest { position ->
                _playbackPosition.value = position
            }
        }

        // Collect playback duration updates
        viewModelScope.launch {
            playbackClient.playbackDurationFlow.collectLatest { duration ->
                _playbackDuration.value = duration
            }
        }
        viewModelScope.launch {
            playbackClient.isBufferingFlow.collectLatest { buffering ->
                _isBuffering.value = buffering
            }
        }
    }

    fun onEvent(event: AudioScreenEvent) {
        when (event) {
            is AudioScreenEvent.OnMiniBarClick -> {
                val currentIndex = playbackClient.currentMediaItemFlow.value?.let { mediaItem ->
                    val playlist = playbackClient.getCurrentPlaylistMusic()
                    playlist.indexOfFirst { it.contentUri.toString() == mediaItem.mediaId }
                } ?: 0
                navigator.navigate(
                    Screens.MusicDetailScreen(
                        selectedSongId = event.selectedSongId,
                        currentIndex = currentIndex
                    )
                )
            }
        }
    }

    private fun setPlaylist(songs: List<Music>, startIndex: Int = 0) {
        viewModelScope.launch {
            playbackClient.setPlaylist(songs, startIndex)
            _uiState.update {
                it.copy(
                    currentSong = songs[startIndex],
                    isPlaying = true
                )
            }
        }
    }

    fun playSong(song: Music) {
        val musicList = _uiState.value.musicList
        val startIndex = musicList.indexOfFirst { it.contentUri == song.contentUri }

        if (startIndex != -1) {
            setPlaylist(musicList, startIndex)
        }
    }

    fun togglePlayPause() {
        viewModelScope.launch {
            val playing = playbackClient.isPlaying()
            if (playing) {
                playbackClient.pause()
            } else {
                playbackClient.resume()
            }
            _uiState.update { it.copy(isPlaying = !playing) }
        }
    }

}