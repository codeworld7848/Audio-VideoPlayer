package com.anand.player.presentation.screen.playerdetailsscreen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil.compose.AsyncImage
import com.anand.core.models.Music
import com.anand.player.presentation.components.ContentWithStatusBar
import com.anand.player.presentation.components.formatTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


@Composable
fun MusicDetailScreen(
    songs: List<Music>,
    currentIndex: Int,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    isBuffering: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSwipe: (Int) -> Unit
) {
    var isDragging by rememberSaveable { mutableStateOf(false) }
    var sliderDragPosition by rememberSaveable { mutableFloatStateOf(0.0f) }
    var seekTargetPosition by remember { mutableStateOf<Long?>(null) }

    val safeDuration = if (duration > 0) duration.toFloat() else 1f
    val sliderValue = when {
        isDragging -> sliderDragPosition
        seekTargetPosition != null -> seekTargetPosition!!.toFloat()
        else -> currentPosition.toFloat()
    }.coerceIn(0f, safeDuration)


    val pagerState = rememberPagerState(initialPage = currentIndex, pageCount = {
        songs.size
    })
    val animationJob = remember { mutableStateOf<Job?>(null) }
    LaunchedEffect(currentIndex) {
        animationJob.value?.cancel()
        animationJob.value = launch {
            // Only animate if distance is small (optional)
            val distance = (pagerState.currentPage - currentIndex).absoluteValue
            if (distance <= 1) {
                pagerState.animateScrollToPage(
                    currentIndex,
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
            } else {
                pagerState.scrollToPage(currentIndex) // Skip animation for rapid jumps
            }
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != currentIndex) {
            onSwipe(pagerState.currentPage)
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val currentSong = songs.getOrNull(pagerState.currentPage) ?: return

    ContentWithStatusBar(statusBarColor = MaterialTheme.colorScheme.surface) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            IconButton(onClick = {}) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = null)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Swipe only album art
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 60.dp), // <-- Important
                ) { page ->
                    val pageOffset = (
                            (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                            ).absoluteValue

                    val scale = lerp(
                        start = 0.80f,
                        stop = 1f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    )

                    Card(
                        elevation = CardDefaults.cardElevation(10.dp),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .width(250.dp) // <-- Important: limit card width
                            .height(400.dp),
                    ) {
                        AsyncImage(
                            model = songs[page].albumArtUri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.FillBounds
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    currentSong.title, style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )
                Text(
                    currentSong.artist, style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(24.dp))

                Slider(
                    value = sliderValue,
                    onValueChange = {
                        isDragging = true
                        sliderDragPosition = it
                    },
                    onValueChangeFinished = {
                        isDragging = false
                        seekTargetPosition = sliderDragPosition.toLong()
                        onSeekTo(sliderDragPosition.toLong())

                        // Clear seek target after short delay
                        coroutineScope.launch {
                            delay(500) // Or until player updates state
                            seekTargetPosition = null
                        }
                    },
                    valueRange = 0f..safeDuration,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTime(sliderValue.toLong()))
                    Text(formatTime(duration))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        modifier = Modifier
                            .padding(end = 20.dp),
                        onClick = {
                            onPrevious.invoke()
                        }, enabled = !isBuffering
                    ) {
                        Icon(
                            modifier = Modifier.size(40.dp),
                            painter = painterResource(androidx.media3.session.R.drawable.media3_icon_previous),
                            contentDescription = "Previous"
                        )
                    }

                    var isButtonEnabled by remember { mutableStateOf(true) }

                    IconButton(
                        modifier = Modifier.padding(end = 20.dp),
                        onClick = {
                            if (isButtonEnabled) {
                                isButtonEnabled = false
                                onPlayPause()
                                // Re-enable after a short delay or when playback state updates
                                coroutineScope.launch {
                                    delay(300)
                                    isButtonEnabled = true
                                }
                            }
                        }, enabled = !isBuffering && isButtonEnabled
                    ) {
                        if (isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                modifier = Modifier.size(50.dp),
                                painter = if (isPlaying) painterResource(androidx.media3.session.R.drawable.media3_icon_pause) else painterResource(
                                    androidx.media3.session.R.drawable.media3_icon_play
                                ),
                                contentDescription = "Play/Pause"
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            onNext.invoke()
                        }, enabled = !isBuffering
                    ) {
                        Icon(
                            modifier = Modifier.size(40.dp),
                            painter = painterResource(androidx.media3.session.R.drawable.media3_icon_next),
                            contentDescription = "Next"
                        )
                    }
                }
            }
        }
    }
}
