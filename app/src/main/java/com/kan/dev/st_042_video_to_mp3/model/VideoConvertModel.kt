package com.kan.dev.st_042_video_to_mp3.model

import android.net.Uri

data class VideoConvertModel(
    val uri: Uri,
    val duration: String?,
    val sizeInMB: String?,
    val name: String
 )