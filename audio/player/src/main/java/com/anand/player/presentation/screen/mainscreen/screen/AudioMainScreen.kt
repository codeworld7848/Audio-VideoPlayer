package com.anand.player.presentation.screen.mainscreen.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.anand.player.presentation.components.ContentWithStatusBar
import com.anand.player.presentation.components.MiniPlayerBar
import com.anand.player.presentation.screen.mainscreen.events.AudioScreenEvent
import com.anand.player.presentation.screen.mainscreen.viewmodel.AudioScreenViewModel

@Composable
fun AudioMainScreen(
    audioScreenViewModel: AudioScreenViewModel = hiltViewModel()
) {
    val uiState by audioScreenViewModel.uiState.collectAsState()
    ContentWithStatusBar(statusBarColor = androidx.compose.material3.MaterialTheme.colorScheme.surface) {
        Box(
            Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter
        ) {
            Box (
                modifier = Modifier
                    .fillMaxSize(),
              contentAlignment = Alignment.BottomCenter
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()

                ) {
                    items(uiState.musicList) { song ->
                        ListItem(headlineContent = { Text(song.title) },
                            supportingContent = { Text(song.artist) },
                            leadingContent = {
                                AsyncImage(
                                    model = song.albumArtUri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            },
                            modifier = Modifier.clickable {
                                audioScreenViewModel.playSong(song)
                            })
                    }
                    item {
                        uiState.currentSong?.let {
                            Box(modifier = Modifier.fillMaxWidth().height(70.dp).background(ListItemDefaults.containerColor))
                        }
                    }
                }
                val isBuffering by audioScreenViewModel.isBuffering.collectAsState()
                uiState.currentSong?.let { song ->
                    MiniPlayerBar(
                        song = song,
                        isPlaying = uiState.isPlaying,
                        isBuffering = isBuffering,
                        onPlayPause = { audioScreenViewModel.togglePlayPause() },
                        onClick = {
                            audioScreenViewModel.onEvent(
                                AudioScreenEvent.OnMiniBarClick(
                                    song.id
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}