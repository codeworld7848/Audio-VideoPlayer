package com.anand.player.service

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.anand.core.models.Music
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackClient @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var controller: MediaController? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val _isPlaying = MutableStateFlow(false)
    val isPlayingFlow: StateFlow<Boolean> = _isPlaying

    private val _currentMediaItem = MutableStateFlow<MediaItem?>(null)
    val currentMediaItemFlow: StateFlow<MediaItem?> = _currentMediaItem

    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPositionFlow: StateFlow<Long> = _playbackPosition

    private val _playbackDuration = MutableStateFlow(0L)
    val playbackDurationFlow: StateFlow<Long> = _playbackDuration

    private val _isBuffering = MutableStateFlow(false)
    val isBufferingFlow: StateFlow<Boolean> = _isBuffering

    private var positionUpdateJob: Job? = null
    private var currentPlaylist: List<String> = emptyList() // contentUri strings
    private var currentPlaylistMusic: List<Music> = emptyList()

    private suspend fun initController() {
        if (controller == null) {
            controller = MediaController.Builder(
                context,
                SessionToken(context, ComponentName(context, PlaybackService::class.java))
            ).buildAsync().await()

            controller?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                    if (isPlaying) startPositionUpdates() else stopPositionUpdates()
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    _currentMediaItem.value = mediaItem
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    Log.d("PlaybackClient", "onPlaybackStateChanged: $playbackState")
                    _isBuffering.value = playbackState == Player.STATE_BUFFERING
                }
            })
        }
    }


    private fun startPositionUpdates() {
        stopPositionUpdates() // Cancel previous job if any
        positionUpdateJob = coroutineScope.launch {
            while (true) {
                val currentPosition = controller?.currentPosition ?: 0L
                _playbackPosition.value = currentPosition
                // duration can also change dynamically, update if needed
                _playbackDuration.value = controller?.duration ?: 0L
                delay(500L)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    suspend fun setPlaylist(songs: List<Music>, startIndex: Int = 0) {
        initController()
        val mediaItems = songs.map { buildMediaItem(it) }
        controller?.apply {
            setMediaItems(mediaItems, startIndex, 0L)
            prepare()
            play()
        }
        currentPlaylistMusic = songs
        currentPlaylist = songs.map { it.contentUri.toString() } // <--- store current list
    }

    fun getCurrentPlaylistMusic(): List<Music> = currentPlaylistMusic


    fun isSamePlaylist(songs: List<Music>): Boolean {
        val newList = songs.map { it.contentUri.toString() }
        return newList == currentPlaylist
    }

    suspend fun next() {
        initController()
        controller?.seekToNext()
    }

    suspend fun previous() {
        initController()
        controller?.seekToPrevious()
    }

    suspend fun seekTo(positionMs: Long) {
        initController()
        controller?.seekTo(positionMs)
    }

    suspend fun pause() {
        initController()
        controller?.pause()
    }

    suspend fun resume() {
        initController()
        controller?.play()
    }

    suspend fun isPlaying(): Boolean {
        initController()
        return controller?.isPlaying ?: false
    }

    private fun buildMediaItem(song: Music): MediaItem {
        return MediaItem.Builder()
            .setMediaId(song.contentUri.toString())  // explicitly set mediaId as Uri string
            .setUri(song.contentUri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setArtworkUri(song.albumArtUri)
                    .build()
            )
            .build()
    }

}
