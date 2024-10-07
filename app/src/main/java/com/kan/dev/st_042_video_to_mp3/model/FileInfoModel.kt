package com.kan.dev.st_042_video_to_mp3.model

data class FileInfoModel (
    val fileName: String,
    val fileSize: String?,
    val duration: String? // Thêm thông tin duration
)