package com.kan.dev.st_042_video_to_mp3.ui.file_convert_to_mp3

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.MediaCodec
import android.media.MediaCodecList
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.arthenica.mobileffmpeg.FFmpeg
//import com.arthenica.mobileffmpeg.FFmpeg
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivityFileConvertToMp3Binding
import com.kan.dev.st_042_video_to_mp3.model.AudioInfo
import com.kan.dev.st_042_video_to_mp3.model.AudioSpeedModel
import com.kan.dev.st_042_video_to_mp3.model.VideoConvertModel
import com.kan.dev.st_042_video_to_mp3.ui.saved.SavedActivity
import com.kan.dev.st_042_video_to_mp3.utils.AudioUtils
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.Const.audioInfo
import com.kan.dev.st_042_video_to_mp3.utils.Const.audioInformation
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSizeVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.countVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioMerger
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioSaved
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioStorage
import com.kan.dev.st_042_video_to_mp3.utils.Const.listConvertMp3
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.mp3Uri
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionVideoPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectType
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectTypeAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.videoConvert
import com.kan.dev.st_042_video_to_mp3.utils.Const.videoCutter
import com.kan.dev.st_042_video_to_mp3.utils.FileInfo
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.AbsBaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

class FileConvertToMp3Activity : AbsBaseActivity<ActivityFileConvertToMp3Binding>(false) {
    override fun getFragmentID(): Int = 0
    override fun getLayoutId(): Int = R.layout.activity_file_convert_to_mp3
    lateinit var adapter: FileConvertAdapter
    private var exoPlayer: ExoPlayer? = null
    var videoUri: Uri? = null
    private lateinit var mediaPlayer: MediaPlayer
    var videoPath: String? = null
    private var job: Job? = null
    private var isConverting = false
    var outputPath = ""
    var shouldCancel = false
    var listOutputPath = mutableListOf<File>()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun init() {
        checkSupportedCodecs()
        initData()
        initAction()
        binding.tvVidoeToMp3.isSelected = true
        if (listVideoPick.size == 1) {
            initViewFile()
            initDataFile()
        } else {
            initViewMutil()
            initDataMulti()
            initActionMulti()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initData() {

        if (selectType.equals("VideoCutter")) {
            videoUri = videoCutter!!.uri
            Log.d("check_kfkfjfkf", "initData: " + videoUri)
        } else if (listVideo.size > 0 && selectType.equals("Video")) {
            videoUri = Uri.parse(listVideo[positionVideoPlay].uri.toString())
            Log.d("check_mp3", "initData: " + videoUri)
        }
    }

    @OptIn(UnstableApi::class)
    private fun initDataFile() {
        Log.d("check_file", "initDataFile: " + listVideo[positionVideoPlay])
        exoPlayer = ExoPlayer.Builder(this).build()
        binding.exoVideo.player = exoPlayer
        binding.playerControlView.player = exoPlayer
        binding.playerControlView.showTimeoutMs = 3000  // Thiết lập thời gian hiển thị
        val mediaItem = MediaItem.fromUri(videoUri!!)
        exoPlayer!!.setMediaItem(mediaItem)
        exoPlayer!!.prepare()
//        exoPlayer!!.play()
        exoPlayer!!.playWhenReady = false
    }

    private fun initActionMulti() {
        adapter.onClickListener(object : FileConvertAdapter.onClickItemListener {
            override fun onItemClick(position: Int) {
                Log.d("check_data_size", "onItemClick: " + listVideoPick.size)
                if (listVideoPick.size == 1) {
                    Toast.makeText(
                        this@FileConvertToMp3Activity,
                        getString(R.string.items_cannot_be_deleted_you_need_at_least_1_items_to_convert),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val pos = listVideoPick[position].pos
                    listVideo[pos].active = false
                    countVideo -= 1
                    countSizeVideo -= listVideo[pos].sizeInMB.toInt()
                    listVideoPick.removeAt(position)
                    adapter.notifyDataSetChanged()
                }
            }
        })
    }

    @SuppressLint("WrongConstant")
    fun extractAudioFromVideo(videoPath: String, outputAudioPath: String) {
        val extractor = MediaExtractor()
        extractor.setDataSource(videoPath)

        val audioTrackIndex = getAudioTrackIndex(extractor)
        if (audioTrackIndex >= 0) {
            extractor.selectTrack(audioTrackIndex)

            val mediaFormat = extractor.getTrackFormat(audioTrackIndex)
            val muxer = MediaMuxer(outputAudioPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val trackIndex = muxer.addTrack(mediaFormat)

            muxer.start()

            val buffer = ByteBuffer.allocate(1024 * 1024)
            val info = MediaCodec.BufferInfo()

            while (true) {
                info.offset = 0
                info.size = extractor.readSampleData(buffer, 0)
                if (info.size < 0) break

                info.presentationTimeUs = extractor.sampleTime
                info.flags = extractor.sampleFlags

                muxer.writeSampleData(trackIndex, buffer, info)
                extractor.advance()
            }
            muxer.stop()
            muxer.release()
        }

        extractor.release()
    }

    fun playAudio(audioPath: String) {
        // Đường dẫn đến file M4A
        mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setDataSource(audioPath) // Đặt nguồn phát
            mediaPlayer.prepare() // Chuẩn bị phát
            mediaPlayer.start() // Bắt đầu phát
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAudioTrackIndex(extractor: MediaExtractor): Int {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime != null) {
                if (mime.startsWith("audio/")) {
                    return i
                }
            }
        }
        return -1
    }

    @SuppressLint("ClickableViewAccessibility")
    @OptIn(UnstableApi::class)
    private fun initAction() {
        binding.loadingOverlay.setOnTouchListener { _, _ ->
            true
        }

        binding.exoVideo.setOnClickListener {
            binding.playerControlView.show()
        }

        binding.playerControlView.setOnClickListener {
            binding.playerControlView.hide()
        }

        binding.imvBack.onSingleClick {
            finish()
        }
        binding.LnConvert.onSingleClick {
            shouldCancel = false
            clearCache()
            showLoadingOverlay()
            job = CoroutineScope(Dispatchers.Main).launch {
                convertAllSongsToMp3()
            }
//            if (listVideoPick.size == 1) {
//                if (exoPlayer != null) {
//                    exoPlayer!!.pause()
//                }
//                if (selectType.equals("VideoCutter")) {
//                    videoPath = videoCutter!!.uri.toString()
//                    Log.d("check_path", "initAcrjngrgrgtion: " + videoPath)
//                    selectType = "VideoCutterToMp3"
//                } else {
//                    videoPath = getRealPathFromURI(this, videoUri!!)
//                }
//                val timestamp = System.currentTimeMillis()
//                val tempAudioPath = "${cacheDir.absolutePath}/temp_audio_${timestamp}.m4a"
////                val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
//                outputPath = "${cacheDir.path}/converter_cache_${timestamp}.mp3"
//                videoPath?.let { extractAudioFromVideo(it, tempAudioPath) }
//                Log.d("check_path___ư", "initAcrjngrgrgtion: " + videoPath)
//                videoPath?.let { convertAudio(it,outputPath,"mp3") }
//                playAudio(videoPath.toString())
//                convertToMp3(tempAudioPath, outputPath)
//                convertVideoToMp3(videoPath!!, outputPath)

            // Xóa file tạm thời sau khi chuyển đổi
//                File(tempAudioPath).delete()

//                val timestamp = System.currentTimeMillis()
////                val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
//                Log.d("check_path", "initAction: " + videoPath)
//                outputPath = "${cacheDir.path}/converter_to_mp3_cache_${timestamp}.mp3"
//                if (videoPath != null) {
//                    convertVideoToMp3(videoPath!!, outputPath)
//                }
//            } else {
//                if (!isConverting) {
//                    isConverting = true
//                    job = CoroutineScope(Dispatchers.Main).launch {
//                        listAudioMerger.clear()
//                        listAudioSaved.clear()
//                        convertAllVideosToMp3()
//                    }
//                }
//            }
        }
    }

    private suspend fun convertAllSongsToMp3() {
        listAudioSaved.clear()
        listAudioMerger.clear()
        listOutputPath.clear()
        if (exoPlayer != null) {
            exoPlayer!!.pause()
        }
        if (selectType.equals("VideoCutter")) {
            videoPath = videoCutter!!.uri.toString()
            Log.d("check_path", "initAcrjngrgrgtion: " + videoPath)
            selectType = "VideoCutterToMp3"
        } else {
            videoPath = getRealPathFromURI(this, videoUri!!)
        }
        withContext(Dispatchers.IO) {
            for (video in listVideoPick) {
//                Log.d("check_iust_out_put_path", "convertAllSongsToMp3: _____" + listAudioPick.size)
                val audioPath = getRealPathFromURI(this@FileConvertToMp3Activity, video.uri!!)
                val timestamp = System.currentTimeMillis()
//                val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
                val tempAudioPath = "${cacheDir.absolutePath}/temp_audio_${timestamp}.m4a"
                audioPath?.let { extractAudioFromVideo(it, tempAudioPath) }
                Log.d("check_iust_out_put_path", "convertAllSongsToMp3: _____" + tempAudioPath)
                outputPath = "${cacheDir.path}/converter_cache_${timestamp}.mp3"
                if (audioPath != null) {
                    convertAudio(audioPath, outputPath, "mp3")
                }
                if (shouldCancel) { // shouldCancel là một biến boolean mà bạn có thể đặt điều kiện
                    break // Hủy vòng lặp
                }
            }
        }
        lifecycleScope.launch {
            startActivity(Intent(this@FileConvertToMp3Activity, SavedActivity::class.java))
            delay(500)
            hideLoadingOverlay()
        }
    }

    fun convertAudio(inputPath: String, outputPath: String, format: String) {
        val command = "-i \"$inputPath\" -vn -ar 44100 -ac 2 -b:a 192k \"$outputPath\""
        Log.d("check_mp3", "Chuyển đổi : $command")
        val resultCode = FFmpeg.execute(command)
        if (resultCode == 0) {
            listOutputPath.add(File(outputPath))
            Log.d(
                "check_iust_out_put_path",
                "convertAudio: " + listOutputPath.size + "  " + listVideoPick.size
            )
            if (listOutputPath.size == listVideoPick.size) {
                listOutputPath.forEachIndexed { index, cacheFile ->
                    Log.d(
                        "check_iust_out_put_path",
                        "convertAudio: " + index + "  ____  " + cacheFile
                    )
                    val timestamp = System.currentTimeMillis()
                    val musicDir =
                        File(Environment.getExternalStorageDirectory(), "Music/music")
                    val path = File(
                        "${musicDir.absolutePath}/${
                            listVideo[index].name.substringBeforeLast(".")
                        }_${timestamp}_convert.mp3"
                    )
                    cacheFile.copyTo(path, overwrite = true)
                    var audioInfoConverter =
                        FileInfo.getFileInfoFromPath(Uri.parse(path.toString()).toString())
                    audioInformation = AudioInfo(
                        Uri.parse(path.toString()),
                        audioInfoConverter!!.duration.toString(),
                        convertMbToBytes(
                            audioInfoConverter.fileSize.toString()
                        ),
                        audioInfoConverter.fileName,
                        getFormattedDate(),
                        false,
                        "mp3",
                        0,
                        false
                    )
                    listAudioMerger.add(audioInformation!!)
                    audioInfo = AudioSpeedModel(
                        Uri.parse(path.toString()),
                        audioInfoConverter!!.duration.toString(),
                        audioInfoConverter.fileSize,
                        audioInfoConverter.fileName.toString()
                    )
                    listAudioSaved.add(audioInfo!!)
                }
                Log.d("check---------------", "convertAudio: thahahahahahaaha " + audioInfo)

            }
        } else {
            Log.d("check_mp3", "Chuyển đổi thất bại. Mã lỗi: $resultCode")
        }
    }
    private fun checkSupportedCodecs() {
        val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
        val codecs = codecList.codecInfos
        for (codecInfo in codecs) {
            if (codecInfo.isEncoder) {
                val supportedTypes = codecInfo.supportedTypes
                for (type in supportedTypes) {
                    Log.d("CodecInfo", "Codec: ${codecInfo.name}, Type: $type")
                }
            }
        }
    }
//    private suspend fun convertAllVideosToMp3() {
//        listAudioSaved.clear()
//        listAudioMerger.clear()
//        listOutputPath.clear()
//        withContext(Dispatchers.IO) { // Chạy trong IO context
//            for (video in listVideoPick) {
//                val videoPath = getRealPathFromURI(this@FileConvertToMp3Activity, video.uri)
//                val timestamp = System.currentTimeMillis()
////                val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
//                outputPath = "${cacheDir.path}/converter_to_mp3_cache_${timestamp}.mp3"
//                if (videoPath != null) {
//                    convertVideoToMp3(videoPath, outputPath)
//                }
//                if (shouldCancel) { // shouldCancel là một biến boolean mà bạn có thể đặt điều kiện
//                    break // Hủy vòng lặp
//                }
//            }
//        }
//        lifecycleScope.launch {
//            // Khởi động Activity mới
//            startActivity(Intent(this@FileConvertToMp3Activity, SavedActivity::class.java))
//            // Trì hoãn 500ms trước khi gọi hideLoadingOverlay
//            delay(500)
//            // Gọi hideLoadingOverlay() sau khi đã khởi động Activity mới
//            hideLoadingOverlay()
//        }
////        startActivity(Intent(this, SavedActivity::class.java))
//    }


    private fun showLoadingOverlay() {
        binding.loadingOverlay.visibility = View.VISIBLE
        val animator = ObjectAnimator.ofInt(binding.lottieAnimationView, "progress", 0, 100)
        animator.duration = 1000 // Thời gian chạy animation (5 giây)
        animator.start()
    }

    private fun hideLoadingOverlay() {
        binding.loadingOverlay.visibility = View.GONE
    }

    fun convertVideoToMp3(videoUri: String, outputPath: String) {
        val command = "-i \"$videoUri\" -vn -ar 44100 -ac 2 -b:a 192k \"$outputPath\""
//        val command = "-i \"$videoUri\" -vn -ar 44100 -ac 2 -b:a 192k \"$outputPath\""
//        Log.d("check_iust_out_put_path", "convertAudio: "+  videoUri)
//        val command =   "-i ${videoUri} -codec:a aac -b:a 192k ${outputPath}"
        val resultCode = FFmpeg.execute(command)
        if (resultCode == 0) {
            listOutputPath.add(File(outputPath))
            Log.d(
                "check_iust_out_put_path",
                "convertAudio: " + listOutputPath.size + "  " + listVideoPick.size
            )
            if (listVideoPick.size == 1) {
                val timestamp = System.currentTimeMillis()
                val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
                val path =
                    File("${musicDir.absolutePath}/${listOutputPath[0].name}_${timestamp}_video_converter.mp3")
                val cacheFile = File(outputPath)
                cacheFile.copyTo(path, overwrite = true)
                mp3Uri = Uri.parse(path.toString())
                val infoFile = FileInfo.getFileInfoFromPath(mp3Uri!!.toString())
                videoConvert = VideoConvertModel(
                    mp3Uri!!,
                    infoFile!!.duration,
                    infoFile!!.fileSize,
                    infoFile.fileName
                )
                lifecycleScope.launch {
                    // Khởi động Activity mới
                    startActivity(Intent(this@FileConvertToMp3Activity, SavedActivity::class.java))
                    // Trì hoãn 500ms trước khi gọi hideLoadingOverlay
                    delay(500)
                    // Gọi hideLoadingOverlay() sau khi đã khởi động Activity mới
                    hideLoadingOverlay()
                }
            } else if (listOutputPath.size == listVideoPick.size) {
                listOutputPath.forEachIndexed { index, cacheFile ->
                    Log.d(
                        "check_iust_out_put_path",
                        "convertAudio: " + index + "  ____  " + cacheFile
                    )
                    val timestamp = System.currentTimeMillis()
                    val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
                    val path =
                        File("${musicDir.absolutePath}/${listVideo[index].name.substringBeforeLast(".")}_${timestamp}_convertMp3_$index.mp3")
                    cacheFile.copyTo(path, overwrite = true)
                    var audioInfoConverter =
                        FileInfo.getFileInfoFromPath(Uri.parse(path.toString()).toString())
                    audioInformation = AudioInfo(
                        Uri.parse(path.toString()),
                        audioInfoConverter!!.duration.toString(),
                        convertMbToBytes(
                            audioInfoConverter.fileSize.toString()
                        ),
                        audioInfoConverter.fileName,
                        getFormattedDate(),
                        false,
                        "mp3",
                        0,
                        false
                    )
                    listAudioMerger.add(audioInformation!!)
                    audioInfo = AudioSpeedModel(
                        Uri.parse(path.toString()),
                        audioInfoConverter!!.duration.toString(),
                        audioInfoConverter.fileSize,
                        audioInfoConverter.fileName.toString()
                    )
                    listAudioSaved.add(audioInfo!!)
                    listConvertMp3.add(path.toString())

                    Log.d("check__dkfnehbfebhjf", "convertVideoToMp3: " + listAudioMerger)
                }

            }
        } else {
            Log.d("check_mp3", "Chuyển đổi thất bại. Mã lỗi: $resultCode")
        }
    }


    private fun initViewFile() {
        binding.exoVideo.visibility = View.VISIBLE
        binding.playerControlView.visibility = View.VISIBLE
        binding.tvTitle.text = getString(R.string.convert_to_mp3)
    }

    fun getFormattedDate(): String {
        val currentDate = Date() // Lấy ngày hiện tại
        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault()) // Định dạng ngày
        return dateFormat.format(currentDate) // Trả về chuỗi ngày đã định dạng
    }

    private fun initViewMutil() {
        binding.tvTitle.text = getString(R.string.multifile_convert)
        binding.recFileConvert.visibility = View.VISIBLE
    }
    private fun initDataMulti() {
        adapter = FileConvertAdapter(this@FileConvertToMp3Activity)
        adapter.getData(listVideoPick)
        binding.recFileConvert.adapter = adapter
        Log.d("check_size_list", "initDataMulti: " + listVideoPick.size)
    }
    override fun onDestroy() {
        super.onDestroy()
        clearCache()
        if (exoPlayer != null) {
            exoPlayer?.release() // Giải phóng tài nguyên
        }
        job?.cancel()
    }
//    override fun onStop() {
//        super.onStop()
////        job?.cancel()
////        FFmpeg.cancel()
//
//    }
    override fun onPause() {
        super.onPause()
        if (exoPlayer?.isPlaying == true) {
            // Dừng phát âm thanh nếu đang phát
            exoPlayer?.pause() // Giải phóng tài nguyên
        }
    }
    fun clearCache() {
        val cacheDir = cacheDir
        val files = cacheDir.listFiles()
        if (files != null) {
            for (file in files) {
                file.delete() // Xóa từng tệp
            }
        }
    }
    fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
        var path: String? = null
        val proj = arrayOf(MediaStore.Video.Media.DATA)
        val cursor: Cursor? = context.contentResolver.query(contentUri, proj, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                path = it.getString(columnIndex)
            }
        }
        return path
    }
    fun convertMbToBytes(sizeString: String): Long {
        val numericString = sizeString.replace(" MB", "").replace(",", ".").trim()
        val mbSize = numericString.toDouble()
        return (mbSize*1024).toLong() // Nhân để chuyển đổi từ MB sang bytes
    }
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        FFmpeg.cancel()
        job?.cancel()
        shouldCancel = true
        if (binding.loadingOverlay.visibility == View.VISIBLE) {
            startCoroutine()
            hideLoadingOverlay()
        } else {
            finish()
        }
    }
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onResume() {
        super.onResume()
        selectTypeAudio = "Video"
        isConverting = false
        Log.d("check_list_videoPick", "onResume: " + listVideoPick)
    }
    fun startCoroutine() {
        listOutputPath.clear()
        listAudioSaved.clear()
        listAudioMerger.clear()
        isConverting = false
    }

}