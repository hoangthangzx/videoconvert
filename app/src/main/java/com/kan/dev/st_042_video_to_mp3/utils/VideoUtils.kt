package com.kan.dev.st_042_video_to_mp3.utils

import android.content.ContentResolver
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.kan.dev.st_042_video_to_mp3.model.VideoInfo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoStorage
import com.kan.dev.st_042_video_to_mp3.utils.FileInfo.formatDuration
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object VideoUtils {
    var countVd = 0
//    var countVideoSt = 0
    fun getAllVideosFromSpecificDirectory(directoryPath: String) {
        val directory = File(directoryPath)

        if (directory.exists() && directory.isDirectory) {
            val videoExtensions = listOf("mp4", "mkv", "avi", "mov", "wmv")

            directory.listFiles()?.forEachIndexed { index, file ->
                if (file.isFile && videoExtensions.any { file.name.endsWith(it, ignoreCase = true) }) {
                    val videoUri = Uri.fromFile(file) // Tạo URI từ tệp
                    val duration = getVideoDuration(file) // Gọi hàm lấy thời gian
                    val sizeInMB = file.length() / (1024 * 1024) // Kích thước tệp tính bằng MB
                    val name = file.name
                    val date = formatDate(file.lastModified()) // Định dạng ngày

                    listVideoStorage.add(VideoInfo(videoUri, duration, sizeInMB, name, date, false, index))
                }
            }
        } else {
            Log.e("GetVideos", "Directory does not exist or is not a directory.")
        }

    }

    fun getVideoDuration(file: File): String {
        val retriever = MediaMetadataRetriever()
        var duration = "0:00"
        try {
            retriever.setDataSource(file.absolutePath)
            val durationInMillis = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
            duration = formatDuration(durationInMillis)
        } catch (e: Exception) {
            Log.e("GetVideoDuration", "Error retrieving video duration: ${e.message}")
        } finally {
            retriever.release()
        }
        return duration
    }


//    fun getAllVideosStorage(contentResolver: ContentResolver, specificPath: String) {
//        val uri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//        val projection = arrayOf(
//            MediaStore.Video.Media._ID,
//            MediaStore.Video.Media.DURATION,
//            MediaStore.Video.Media.SIZE,
//            MediaStore.Video.Media.DATE_ADDED,
//            MediaStore.Video.Media.DISPLAY_NAME
//        )
//        val cursor: Cursor? = contentResolver.query(
//            uri,
//            projection,
//            null,
//            null,
//            null
//        )
//
//        cursor?.use {
//            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
//            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
//            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
//            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
//            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
//
//            while (cursor.moveToNext()) {
//                val id = cursor.getLong(idColumn)
//                val videoUri = Uri.withAppendedPath(uri, id.toString())
//                val duration = cursor.getLong(durationColumn)
//                val size = cursor.getLong(sizeColumn)
//                val dateAdded = cursor.getLong(dateAddedColumn) * 1000L
//                val sizeInMB = size / (1024 * 1024)
//                val videoName = cursor.getString(nameColumn)
//                val formattedDate = formatDate(dateAdded)
//
//                if (videoUri.path?.startsWith(specificPath) == true) {
//                    listVideoStorage.add(VideoInfo(videoUri, formatTimeToHoursMinutes(duration), sizeInMB, videoName, formattedDate, false, countVideoSt))
//                    countVideoSt += 1
//                }
//            }
//        }
//    }


    fun getAllVideos(contentResolver: ContentResolver) {
        val uri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DISPLAY_NAME
        )
        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
        )
        cursor?.use {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE) // Lấy index của cột SIZE
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME) // Lấy index của cột tên video

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val videoUri = Uri.withAppendedPath(uri, id.toString())
                val duration = cursor.getLong(durationColumn)
                val size = cursor.getLong(sizeColumn)
                val dateAdded = cursor.getLong(dateAddedColumn) * 1000L// Lấy giá trị dung lượng
                val sizeInMB = size / (1024 * 1024)
                val videoName = cursor.getString(nameColumn) // Lấy giá trị tên video
                val formattedDate = formatDate(dateAdded)
                listVideo.add(VideoInfo(videoUri, formatTimeToHoursMinutes(duration), sizeInMB, videoName,formattedDate ,false, countVd))
                countVd +=1
            }
        }

        Log.d("check_count", "getAllVideos: "+ countVd)

    }

    fun formatTimeToHoursMinutes(duration: Long): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = Date(timestamp)
        return sdf.format(date)
    }

}