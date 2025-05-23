package com.anand.player.presentation.screen.playerdetailsscreen

sealed interface MusicDetailEvent {
    data class LoadSongById(val id: Long, val currentIndex: Int) : MusicDetailEvent
    data object TogglePlayPause : MusicDetailEvent
    data object PlayNext : MusicDetailEvent
    data object PlayPrevious : MusicDetailEvent
    data class SeekTo(val position: Long) : MusicDetailEvent
    data class PlaySongAtIndex(val index: Int) : MusicDetailEvent
}
