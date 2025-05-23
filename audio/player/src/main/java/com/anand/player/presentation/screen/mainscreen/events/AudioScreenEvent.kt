package com.anand.player.presentation.screen.mainscreen.events

sealed interface AudioScreenEvent {
    data class OnMiniBarClick(val selectedSongId: Long) : AudioScreenEvent
}