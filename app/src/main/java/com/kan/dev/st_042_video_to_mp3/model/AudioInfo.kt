package com.kan.dev.st_042_video_to_mp3.model

import android.net.Uri

data class AudioInfo(
    val uri: Uri,
    val duration: String,
    val sizeInMB: Long,
    var name: String,
    val date : String,
    var active: Boolean,
    val mimeType: String,
    var pos: Int,
    var activePl : Boolean
)