package com.anand.audiovideoplayer.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.anand.core.utils.Screens
import com.anand.player.presentation.screen.mainscreen.screen.AudioMainScreen
import com.anand.player.presentation.screen.playerdetailsscreen.MusicDetailEvent
import com.anand.player.presentation.screen.playerdetailsscreen.MusicDetailScreen
import com.anand.player.presentation.screen.playerdetailsscreen.MusicDetailViewModel

@Composable
fun SetUpNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screens.MusicMainScreen
    ) {
        composable<Screens.MusicMainScreen> {
            AudioMainScreen()
        }
        composable<Screens.MusicDetailScreen>(
            enterTransition = {
                slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(1000),
                    initialOffset = {
                        1000
                    })
            }
        ) { backStackEntry ->
            val selectedSongId = backStackEntry.toRoute<Screens.MusicDetailScreen>().selectedSongId
            val currentIndex = backStackEntry.toRoute<Screens.MusicDetailScreen>().currentIndex

            val viewModel: MusicDetailViewModel = hiltViewModel()

            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(Unit) {
                if (!uiState.isInitialized || uiState.lastLoadedSongId != selectedSongId) {
                    viewModel.onEvent(MusicDetailEvent.LoadSongById(selectedSongId, currentIndex))
                }
            }
            if (!uiState.isInitialized) {
                // Show a loading UI or empty Box while waiting for data
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                    // You can show a progress bar or nothing here
                }
            } else {
                MusicDetailScreen(
                    songs = uiState.songList,
                    currentIndex = uiState.currentIndex,
                    isPlaying = uiState.isPlaying,
                    currentPosition = uiState.currentPosition,
                    duration = uiState.duration,
                    isBuffering = uiState.isBuffering,
                    onPlayPause = { viewModel.onEvent(MusicDetailEvent.TogglePlayPause) },
                    onNext = { viewModel.onEvent(MusicDetailEvent.PlayNext) },
                    onPrevious = { viewModel.onEvent(MusicDetailEvent.PlayPrevious) },
                    onSeekTo = { viewModel.onEvent(MusicDetailEvent.SeekTo(it)) },
                    onSwipe = { viewModel.onEvent(MusicDetailEvent.PlaySongAtIndex(it)) },
                    onSelectUpComingSong = { index ->
                        viewModel.onEvent(MusicDetailEvent.PlaySongAtIndex(index))
                    }
                )
            }
        }
    }
}