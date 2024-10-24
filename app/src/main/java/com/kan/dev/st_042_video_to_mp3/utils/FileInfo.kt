package com.kan.dev.st_042_video_to_mp3.utils

import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.util.TimeUtils.formatDuration
import com.kan.dev.st_042_video_to_mp3.model.FileInfoModel
import java.io.File
import java.text.DecimalFormat

object FileInfo {

    fun getFileInfoFromPath(path: String): FileInfoModel? {
        val file = File(path)
        val fileName = file.name // Lấy tên file
        val fileSize = if (file.exists()) {
            formatFileSize(file.length()) // Lấy kích thước file
        } else {
            null // Nếu file không tồn tại
        }
        // Lấy duration của file media (nếu có)
        val mediaRetriever = MediaMetadataRetriever()
        var duration: String? = null
        try {
            mediaRetriever.setDataSource(path)
            val durationStr = mediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration = if (durationStr != null) {
                val durationMillis = durationStr.toLong()
                formatDuration(durationMillis) // Hàm tự viết để format thời gian theo ý bạn
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaRetriever.release()
        }

        return FileInfoModel(fileName, fileSize, duration)
    }

    fun formatFileSizeKB(sizeInBytes: Long): String {
        val kiloBytes = sizeInBytes / 1024
        return "$kiloBytes"
    }

    fun formatDuration(durationMillis: Long): String {
        val seconds = (durationMillis / 1000) % 60
        val minutes = (durationMillis / (1000 * 60)) % 60
        val hours = (durationMillis / (1000 * 60 * 60))

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    // Hàm định dạng kích thước file thành MB
    private fun formatFileSize(sizeInBytes: Long): String {
        val sizeInMB = sizeInBytes.toDouble() / (1024 * 1024) // Chuyển byte thành MB
        val decimalFormat = DecimalFormat("#.##") // Giới hạn 2 chữ số thập phân
        return decimalFormat.format(sizeInMB) + " MB"
    }
}