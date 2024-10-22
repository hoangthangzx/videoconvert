package com.kan.dev.st_042_video_to_mp3.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.kan.dev.st_042_video_to_mp3.model.VideoInfo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoStorage
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionVideoPlay
import com.kan.dev.st_042_video_to_mp3.utils.FileInfo.formatDuration
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object VideoUtils {
    var countVd = 0
    var countVideoSt = 0
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
    fun getAllVideosFromSpecificDirectory(contentResolver: ContentResolver, storageString: String) {
        val uri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DISPLAY_NAME
        )
        val selection = "${MediaStore.Video.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("/storage/emulated/0/Movies/video/%")
        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            null
        )
        Log.d("check_data", "getAllVideosFromSpecificDirectory: " + cursor)
        cursor?.use {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val sizeColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE) // Lấy index của cột SIZE
            val nameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME) // Lấy index của cột tên video

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val videoUri = Uri.withAppendedPath(uri, id.toString())
                val duration = cursor.getLong(durationColumn)
                val size = cursor.getLong(sizeColumn)
                val dateAdded = cursor.getLong(dateAddedColumn) * 1000L// Lấy giá trị dung lượng
                val sizeInMB = size / (1024 * 1024)
                val videoName = cursor.getString(nameColumn) // Lấy giá trị tên video
                val formattedDate = formatDate(dateAdded)
                listVideoStorage.add(
                    VideoInfo(
                        videoUri,
                        formatTimeToHoursMinutes(duration),
                        sizeInMB,
                        videoName,
                        formattedDate,
                        false,
                        countVd
                    )
                )
                countVd += 1
            }
        }

        Log.d("check_count", "getAllVideos: " + countVd)
    }

    fun saveVideoToMediaStore(context: Context, outputPath: String, audioType: String) {
        val file = File(outputPath)

        // Kiểm tra xem file có tồn tại không
        if (!file.exists()) {
            Log.d("check_video", "Video không tồn tại tại đường dẫn: $outputPath")
            return
        }

        // Tạo ContentValues để lưu thông tin video
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, file.name) // Tên file hiển thị
            put(MediaStore.Video.Media.MIME_TYPE, "video/$audioType") // Loại MIME
            put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/video") // Đường dẫn tương đối
        }

        // Chèn video vào MediaStore
        val uri: Uri? = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)

        // Kiểm tra kết quả chèn
        if (uri != null) {
            // Ghi nội dung video vào MediaStore
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                FileInputStream(file).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            Log.d("check_video", "Video đã được lưu vào MediaStore: $uri")
        } else {
            Log.d("check_video", "Lưu video vào MediaStore thất bại")
        }
    }
    fun renameVideoFile(context: Context, videoUri: Uri, newName: String) {
        // Xác định phần đuôi mở rộng dựa trên MIME type
        val extension = when (val mimeType = context.contentResolver.getType(videoUri)) {
            "video/mp4" -> ".mp4"
            "video/3gpp" -> ".3gp"
            "video/quicktime" -> ".mov"
            "video/x-flv" -> ".flv"
            "video/x-matroska" -> ".mkv"
            "video/x-msvideo" -> ".avi"
            "video/mp4v-es" -> ".m4v"
            "video/mpeg" -> ".mts"
            "video/mp2t" -> ".m2ts"
            "video/mp2t" -> ".ts"
            else -> ".mp4" // Mặc định nếu không xác định được loại
        }

        // Tạo tên mới với phần đuôi mở rộng
        val newNameWithExtension = if (newName.endsWith(extension)) {
            newName
        } else {
            "$newName$extension"
        }

        listVideoStorage[positionVideoPlay].name = newNameWithExtension

        // Tạo ContentValues với tên mới
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, newNameWithExtension)
        }

        // Sử dụng ContentResolver để cập nhật
        val rowsUpdated = context.contentResolver.update(videoUri, contentValues, null, null)

        if (rowsUpdated != null && rowsUpdated > 0) {
            Log.d("RenameVideo", "Video renamed successfully to $newNameWithExtension")
        } else {
            Log.e("RenameVideo", "Failed to rename video or video not found")
        }
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
//                    listVideoStorage.add(VideoInfo(videoUri, formatTimeToHoursMinutes(duration), sizeInMB, videoName, formattedDate, false, countVd))
//                    countVd += 1
//                }
//            }
//        }
//    }

    private fun hasAudioTrack(videoUri: Uri, contentResolver: ContentResolver): Boolean {
        val extractor = MediaExtractor()
        return try {
            extractor.setDataSource(contentResolver.openFileDescriptor(videoUri, "r")?.fileDescriptor!!)
            val trackCount = extractor.trackCount
            for (i in 0 until trackCount) {
                val format: MediaFormat = extractor.getTrackFormat(i)
                val mimeType = format.getString(MediaFormat.KEY_MIME) ?: continue
                // Kiểm tra nếu MIME type chứa "audio"
                if (mimeType.startsWith("audio/")) {
                    return true
                }
            }
            false
        } catch (e: Exception) {
            false
        } finally {
            extractor.release()
        }
    }


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
            val sizeColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE) // Lấy index của cột SIZE
            val nameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME) // Lấy index của cột tên video

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val videoUri = Uri.withAppendedPath(uri, id.toString())
                val duration = cursor.getLong(durationColumn)
                val size = cursor.getLong(sizeColumn)
                val dateAdded = cursor.getLong(dateAddedColumn) * 1000L// Lấy giá trị dung lượng
                val sizeInMB = size / (1024 * 1024)
                val videoName = cursor.getString(nameColumn) // Lấy giá trị tên video
                val formattedDate = formatDate(dateAdded)
//                listVideo.add(
//                    VideoInfo(
//                        videoUri,
//                        formatTimeToHoursMinutes(duration),
//                        sizeInMB,
//                        videoName,
//                        formattedDate,
//                        false,
//                        countVd
//                    )
//                )
//                countVd += 1
                if (size > 0 && duration > 1000 && hasAudioTrack(videoUri, contentResolver)) {
                    listVideo.add(
                        VideoInfo(
                            videoUri,
                            formatTimeToHoursMinutes(duration),
                            sizeInMB,
                            videoName,
                            formattedDate,
                            false,
                            countVd
                        )
                    )
                    countVd += 1
                }
            }
        }

        Log.d("check_count", "getAllVideos: " + countVd)

    }



    fun formatTimeToHoursMinutes(duration: Long): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun formatTimeToHoursMinutes_1(duration: Int): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }


    fun formatMilliseconds(milliseconds: Long): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / (1000 * 60)) % 60

        return String.format("%02d:%02d", minutes, seconds)
    }


    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = Date(timestamp)
        return sdf.format(date)
    }

}