package com.anand.core.utils

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

sealed interface Screens {
    @Serializable
    data object MusicMainScreen : Screens

    @Keep
    @Serializable
    data class MusicDetailScreen(
        val selectedSongId: Long,
        val currentIndex: Int
    ) : Screens
}