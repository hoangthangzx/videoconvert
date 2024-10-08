package com.kan.dev.st_042_video_to_mp3.utils

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.kan.dev.st_042_video_to_mp3.model.AudioInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AudioUtils {
    var countPos = 0
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

                Const.listAudio.add(AudioInfo(audioUri, formatTimeToHoursMinutes(duration), sizeInMB, audioName, formattedDate, false, mimeType, countPos))
                countPos+=1
            }
        }
    }

    private fun formatTimeToHoursMinutes(duration: Long): String {
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