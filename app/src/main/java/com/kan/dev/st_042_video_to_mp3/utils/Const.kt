package com.kan.dev.st_042_video_to_mp3.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.model.AudioInfo
import com.kan.dev.st_042_video_to_mp3.model.AudioSpeedModel
import com.kan.dev.st_042_video_to_mp3.model.ElementCount
import com.kan.dev.st_042_video_to_mp3.model.VideoConvertModel
import com.kan.dev.st_042_video_to_mp3.model.VideoCutterModel
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
    val STORAGE_PERMISSION_CODE = 1
    val REQUEST_CODE_NOTIFICATION_POLICY = 3
    var listVideo = mutableListOf<VideoInfo>()
    var listVideoT = mutableListOf<VideoInfo>()
    var listVideoF = mutableListOf<VideoInfo>()
    var listVideoStorage = mutableListOf<VideoInfo>()
    var listAudioStorage = mutableListOf<AudioInfo>()
    var listAudio = mutableListOf<AudioInfo>()
    var listAudioPick = mutableListOf<AudioInfo>()
    var listAudioMerger = mutableListOf<AudioInfo>()
    var listVideoPick = mutableListOf<VideoInfo>()
    val countMap = mutableMapOf<String, Int>()
    var elementCounts: MutableList<ElementCount> = countMap.map { (name, count) ->
        ElementCount(name, count) // Dùng uri placeholder cho mục đích hiển thị
    }.toMutableList()
    var checkData = false
    var checkDataAudio = false
    var positionVideoPlay = 0
    var positionAudioPlay = 0
    var mp3Uri : Uri? = null
    var uriPlay : Uri? = null
    var uriPlayAll : String? = ""
    var currentRingtone = 0
    var listAudioPickMerger : MutableList<AudioInfo> = mutableListOf()
    var VideoUriSpeed : Uri? = null
    var countAudio = 0
    var countSize = 0
    var clickItem = false
    var countVideo = 0
    var countSizeVideo = 0
    var isTouchEventHandled = false
    var selectType = ""
    var selectTypeAudio = ""
    var audioInfo : AudioSpeedModel? = null
    var videoInfo : VideoInfo? = null
    var audioInformation : AudioInfo? = null
    var videoCutter : VideoCutterModel? = null
    var audioCutter : VideoCutterModel? = null
    var videoConvert : VideoConvertModel? = null
    var listConvertMp3 = mutableListOf<String>()
    var checkDone = false
    var listAudioSaved = mutableListOf<AudioSpeedModel>()
    var musicStorage : File? = null
    var videoStorage : File? = null
    var checkType : Boolean = false
    var selectFr = "Fr"
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