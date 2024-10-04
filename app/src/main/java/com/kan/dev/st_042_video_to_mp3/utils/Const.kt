package com.kan.dev.st_042_video_to_mp3.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.model.AudioInfo
import com.kan.dev.st_042_video_to_mp3.model.VideoInfo
import com.metaldetector.golddetector.finder.model.LanguageModel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

object Const {
    var SPLASH_DELAY = 2500L
    var positionLanguageOld = 0
    val LANGUAGE: String = "tjii6tyh5"
    var Interact : String ="fghjkl"
    val PERMISSION: String = "krkgeas"
    val STORAGE_PERMISSION_CODE = 1
    val REQUEST_CODE_NOTIFICATION_POLICY = 3
    var listVideo = mutableListOf<VideoInfo>()
    var listAudio = mutableListOf<AudioInfo>()
    var listAudioPick = mutableListOf<AudioInfo>()
    var listVideoPick = mutableListOf<VideoInfo>()
    var checkData = false
    var checkDataAudio = false
    var positionVideoPlay = 0
    var positionAudioPlay = 0
    var mp3Uri : Uri? = null
    var VideoUriSpeed : Uri? = null
    var countAudio = 0
    var countSize = 0
    var countVideo = 0
    var countSizeVideo = 0
    var selectType = ""
    var selectTypeAudio = ""
    var audioInfo : AudioInfo? = null
    var videoInfo : VideoInfo? = null
    var musicStorage : File? = null
    var videoStorage : File? = null
    var checkType : Boolean = false
    var listLanguage = mutableListOf<LanguageModel>(
        LanguageModel("Spanish", "es", R.drawable.ic_flag_spanish),
        LanguageModel("French", "fr", R.drawable.ic_flag_french),
        LanguageModel("Hindi", "hi", R.drawable.ic_flag_hindi),
        LanguageModel("English", "en", R.drawable.ic_flag_english),
        LanguageModel("Portuguese", "pt", R.drawable.ic_flag_portugeese),
        LanguageModel("German", "de", R.drawable.ic_flag_germani),
        LanguageModel("Indonesian", "in", R.drawable.ic_flag_indo)
    )



    fun saveMp3ToStorage(context: Context, inputFilePath: String, outputFileName: String): Boolean {
        try {
            // Thư mục Public Music trên bộ nhớ ngoài
            val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)

            // Tạo thư mục nếu chưa có
            if (!musicDir.exists()) {
                musicDir.mkdirs()
            }

            // Đường dẫn đầu ra cho file MP3
            val outputFile = File(musicDir, outputFileName)

            // Copy tệp từ inputFilePath vào outputFile
            FileInputStream(inputFilePath).use { inputStream ->
                FileOutputStream(outputFile).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (inputStream.read(buffer).also { length = it } > 0) {
                        outputStream.write(buffer, 0, length)
                    }
                }
            }

            // Lưu thành công
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }
}