package com.anand.player.service

import android.app.PendingIntent
import android.os.Build
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@OptIn(UnstableApi::class)
class PlaybackService : MediaLibraryService() {

    private var mediaLibrarySession: MediaLibrarySession? = null
    private lateinit var player: ExoPlayer

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()

        val callback = object : MediaLibrarySession.Callback {
            override fun onGetLibraryRoot(
                session: MediaLibrarySession,
                browser: MediaSession.ControllerInfo,
                params: LibraryParams?
            ): ListenableFuture<LibraryResult<MediaItem>> {
                val rootItem = MediaItem.Builder()
                    .setMediaId("root")
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle("Audio Player Root")
                            .build()
                    )
                    .build()

                return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params))
            }

            override fun onCustomCommand(
                session: MediaSession,
                controller: MediaSession.ControllerInfo,
                customCommand: SessionCommand,
                args: Bundle
            ): ListenableFuture<SessionResult> {
                return when (customCommand.customAction) {
                    "custom_close" -> {
                        player.stop()
                        session.release()
                        stopSelf() // Stop the foreground service
                        Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    }

                    else -> super.onCustomCommand(session, controller, customCommand, args)
                }
            }
        }
        mediaLibrarySession = MediaLibrarySession.Builder(this, player, callback)
            .setId("player_session")
            .setSessionActivity(buildSessionActivity()) // ✅ Dynamically opens the app
            .build()

        setMediaNotificationProvider(DefaultMediaNotificationProvider(this)) // Use the custom provider
    }


    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaLibrarySession
    }

    private fun buildSessionActivity(): PendingIntent {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)!!
        return launchIntent.let {
            PendingIntent.getActivity(
                this@PlaybackService,
                0,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    override fun onDestroy() {
        player.release()
        mediaLibrarySession?.release()
        super.onDestroy()
    }
}
