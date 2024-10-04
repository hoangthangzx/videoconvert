package com.kan.dev.st_042_video_to_mp3.model

import android.net.Uri

data class VideoInfo(
    val uri: Uri,
    val duration: String,
    val sizeInMB: Long,
    val name: String,
    var active: Boolean,
    var pos : Int
)