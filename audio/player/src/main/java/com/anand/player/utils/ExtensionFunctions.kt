package com.anand.player.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.anand.player.service.PlaybackService

fun Context.startPlaybackService(mediaUri: Uri) {
    val intent = Intent(this, PlaybackService::class.java).apply {
        action = "ACTION_PLAY"
        putExtra("media_uri", mediaUri.toString())
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(intent)
    } else {
        startService(intent)
    }
}
