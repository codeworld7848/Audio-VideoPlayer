package com.anand.player.presentation.screen.mainscreen.events

import com.anand.core.models.Music

data class AudioScreenState(
    val musicList: List<Music> = emptyList(),
    val currentSong:Music? = null,
    val isPlaying:Boolean = false
)
