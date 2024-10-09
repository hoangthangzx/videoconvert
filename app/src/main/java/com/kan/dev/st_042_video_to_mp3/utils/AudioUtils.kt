package com.kan.dev.st_042_video_to_mp3.utils

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.kan.dev.st_042_video_to_mp3.model.AudioInfo
import com.kan.dev.st_042_video_to_mp3.model.VideoInfo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioStorage
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoStorage
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionAudioPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionVideoPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.typefr
import com.kan.dev.st_042_video_to_mp3.utils.FileInfo.formatDuration
import com.kan.dev.st_042_video_to_mp3.utils.VideoUtils.getVideoDuration
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.reflect.typeOf

object AudioUtils {
    var countPos = 0

    fun getAllAudiosFromSpecificDirectory(directoryPath: String) {
        val directory = File(directoryPath)
        if (directory.exists() && directory.isDirectory) {
            val audioExtensions = listOf("mp3", "wav", "aac", "ogg", "flac")
            directory.listFiles()?.forEachIndexed { index, file ->
                if (file.isFile && audioExtensions.any { file.name.endsWith(it, ignoreCase = true) }) {
                    val audioUri = Uri.fromFile(file) // Tạo URI từ tệp
                    val duration = getAudioDuration(file) // Gọi hàm lấy thời gian
                    val sizeInMB = file.length() / (1024 * 1024) // Kích thước tệp tính bằng MB
                    val name = file.name
                    val mimeType = file.extension
                    val date = formatDate(file.lastModified()) // Định dạng ngày
                    listAudioStorage.add(AudioInfo(audioUri, duration, sizeInMB, name, date, false,mimeType, index, false))
                }
            }
        } else {
            Log.e("GetAudios", "Directory does not exist or is not a directory.")
        }
    }
    fun getAudioDuration(file: File): String {
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


    fun getAllAudios(contentResolver: ContentResolver) {
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.MIME_TYPE
        )

        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE) // Cột MIME type


            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val audioUri = Uri.withAppendedPath(uri, id.toString())
                val duration = cursor.getLong(durationColumn)
                val size = cursor.getLong(sizeColumn)
                val sizeInMB = size / 1024
                val audioName = cursor.getString(nameColumn)
                val dateAdded = cursor.getLong(dateAddedColumn) * 1000L
                val formattedDate = formatDate(dateAdded)
                val mimeType = cursor.getString(mimeTypeColumn)

                Const.listAudio.add(AudioInfo(audioUri, formatTimeToHoursMinutes(duration), sizeInMB, audioName, formattedDate, false, mimeType, countPos,false))
                countPos+=1
            }
        }
    }

    fun renameFile(context: Context, currentFilePath: String, newFileNameWithoutExtension: String): Boolean {
//        val currentFile = File(currentFilePath)
        val filePath = currentFilePath.removePrefix("file:")

        // Khởi tạo đối tượng File từ đường dẫn
        val currentFile = File(filePath.trim())
        val fileExtension = currentFile.extension

        Log.d("check_file", "renameFile: "+ currentFile)
        val newFileName = "$newFileNameWithoutExtension.$fileExtension"
        val newFile = File(currentFile.parent, newFileName)

        return if (currentFile.exists() && currentFile.isFile) {
            // Kiểm tra xem tệp với tên mới (bao gồm cả phần mở rộng) đã tồn tại hay chưa
            if (newFile.exists()) {
                Toast.makeText(context, "Tên tệp đã tồn tại!", Toast.LENGTH_SHORT).show()
                false
            } else {
                // Đổi tên tệp
                if (currentFile.renameTo(newFile)) {
                    Toast.makeText(context, "Đổi tên tệp thành công thành ${newFile.name}", Toast.LENGTH_SHORT).show()
                    if(typefr.equals("vd")){
                        listVideoStorage[positionVideoPlay].name = newFileName
                    }else{
                        listAudioStorage[positionAudioPlay].name = newFileName
                    }

                    true
                } else {
                    Toast.makeText(context, "Đổi tên tệp ${currentFile.name} thất bại", Toast.LENGTH_SHORT).show()
                    false
                }
            }
        } else {
            Toast.makeText(context, "Tệp không tồn tại hoặc không phải là tệp.", Toast.LENGTH_SHORT).show()
            false
        }
    }


    private fun formatTimeToHoursMinutes(duration: Long): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
//
//    private fun formatDate(timestamp: Long): String {
//        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
//        val date = Date(timestamp)
//        return sdf.format(date)
//    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = Date(timestamp)
        return sdf.format(date)
    }
}