package com.anand.player.presentation.screen.playerdetailsscreen

import com.anand.core.models.Music

data class MusicDetailUiState(
    val songList: List<Music> = emptyList(),
    val currentIndex: Int = 0,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0,
    val duration: Long = 0,
    val isBuffering: Boolean = false,
    val isInitialized: Boolean = false,
    val lastLoadedSongId: Long? = null, // <-- Add this
)