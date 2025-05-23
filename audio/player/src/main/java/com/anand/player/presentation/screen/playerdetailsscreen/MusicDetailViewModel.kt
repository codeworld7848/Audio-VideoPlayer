package com.anand.player.presentation.screen.playerdetailsscreen

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anand.inventory.domain.repository.InventoryRepository
import com.anand.player.service.PlaybackClient
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicDetailViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    @ApplicationContext private val context: Context,
    private val playbackClient: PlaybackClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(MusicDetailUiState())
    val uiState: StateFlow<MusicDetailUiState> = _uiState

    init {
        viewModelScope.launch {
            playbackClient.isPlayingFlow.collectLatest { isPlaying ->
                _uiState.update { it.copy(isPlaying = isPlaying) }
            }
        }

        viewModelScope.launch {
            playbackClient.playbackPositionFlow.collectLatest { position ->
                _uiState.update { it.copy(currentPosition = position) }
            }
        }

        viewModelScope.launch {
            playbackClient.playbackDurationFlow.collectLatest { duration ->
                _uiState.update { it.copy(duration = duration) }
            }
        }

        viewModelScope.launch {
            playbackClient.isBufferingFlow.collectLatest { buffering ->
                _uiState.update { it.copy(isBuffering = buffering) }
            }
        }

        viewModelScope.launch {
            playbackClient.currentMediaItemFlow.collectLatest { mediaItem ->
                mediaItem?.let {
                    val currentIndex = _uiState.value.songList.indexOfFirst { song ->
                        song.contentUri.toString() == it.mediaId
                    }
                    if (currentIndex != -1) {
                        _uiState.update { state ->
                            state.copy(currentIndex = currentIndex,
                                currentPosition = 0)
                        }
                    }
                }
            }
        }
    }

    fun onEvent(event: MusicDetailEvent) {
        when (event) {
            is MusicDetailEvent.LoadSongById -> {
                viewModelScope.launch {
                    val list = inventoryRepository.getAllLocalMusic(context)
                    val index = event.currentIndex.takeIf { it in list.indices }
                        ?: list.indexOfFirst { it.id == event.id }

                    if (index != -1) {
                        val currentMediaId = playbackClient.currentMediaItemFlow.value?.mediaId
                        val isSameSong = currentMediaId == list[index].contentUri.toString()
                        val isSamePlaylist = playbackClient.isSamePlaylist(list)

                        if (!isSamePlaylist || !isSameSong) {
                            playbackClient.setPlaylist(list, index)
                        }

                        _uiState.update {
                            it.copy(songList = list, currentIndex = index, isInitialized = true,
                                currentPosition = 0)
                        }
                    }
                }
            }

            is MusicDetailEvent.TogglePlayPause -> {
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

            is MusicDetailEvent.SeekTo -> {
                viewModelScope.launch {
                    playbackClient.seekTo(event.position)
                }
            }

            MusicDetailEvent.PlayNext -> {
                viewModelScope.launch {
                    playbackClient.next()
                }
            }

            MusicDetailEvent.PlayPrevious -> {
                viewModelScope.launch {
                    playbackClient.previous()
                }
            }

            is MusicDetailEvent.PlaySongAtIndex -> {
                viewModelScope.launch {
                    val songList = _uiState.value.songList
                    if (event.index in songList.indices) {
                        playbackClient.setPlaylist(songList, event.index)
                        _uiState.update {
                            it.copy(
                                currentIndex = event.index,
                                currentPosition = 0
                            )
                        }
                    }
                }
            }
        }
    }
}
