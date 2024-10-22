package com.kan.dev.st_042_video_to_mp3.ui.file_convert_to_mp3

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
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
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.Const.audioInfo
import com.kan.dev.st_042_video_to_mp3.utils.Const.audioInformation
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSizeVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.countVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioMerger
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioSaved
import com.kan.dev.st_042_video_to_mp3.utils.Const.listConvertMp3
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.mp3Uri
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionVideoPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectType
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileConvertToMp3Activity : AbsBaseActivity<ActivityFileConvertToMp3Binding>(false) {
    override fun getFragmentID(): Int = 0
    override fun getLayoutId(): Int = R.layout.activity_file_convert_to_mp3
    lateinit var adapter: FileConvertAdapter
    private var exoPlayer: ExoPlayer? = null
    var videoUri: Uri? = null
    var videoPath: String? = null
    private var job: Job? = null
    var outputPath = ""
    var checkDone = false
    var listOutputPath = mutableListOf<File>()
    private var isConverting = false

    @RequiresApi(Build.VERSION_CODES.N)
    override fun init() {
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

    @OptIn(UnstableApi::class)
    private fun initAction() {
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
            if (listVideoPick.size == 1) {
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
                showLoadingOverlay()
                val timestamp = System.currentTimeMillis()
//                val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
                Log.d("check_path", "initAction: " + videoPath)
                outputPath = "${cacheDir.path}/converter_to_mp3_cache_${timestamp}.mp3"

                if (videoPath != null) {
                    convertVideoToMp3(videoPath!!, outputPath)
                }
            } else {
                if (!isConverting) {
                    showLoadingOverlay()
                    isConverting = true
                    job = CoroutineScope(Dispatchers.Main).launch {
                        listAudioMerger.clear()
                        listAudioSaved.clear()
                        convertAllVideosToMp3()
                    }
                }
            }
        }
    }

    private suspend fun convertAllVideosToMp3() {
        withContext(Dispatchers.IO) { // Chạy trong IO context
            for (video in listVideoPick) {
                val videoPath = getRealPathFromURI(this@FileConvertToMp3Activity, video.uri)
                val timestamp = System.currentTimeMillis()
//                val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
                outputPath = "${cacheDir.path}/converter_to_mp3_cache_${timestamp}.mp3"
                if (videoPath != null) {
                    convertVideoToMp3(videoPath, outputPath)
                }
            }
        }
        startActivity(Intent(this, SavedActivity::class.java))
        isConverting = false
    }

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
        val command = "-i \"$videoUri\" -vn -ar 44100 -ac 2 -b:a 192k $outputPath"
        val resultCode = FFmpeg.execute(command)
        if (resultCode == 0) {
            listOutputPath.add(File(outputPath))
            Log.d("check_iust_out_put_path", "convertAudio: "+ listOutputPath  + listOutputPath.size + "  " + listAudioPick.size)
            if (listVideoPick.size == 1) {
                checkDone = true
                val timestamp = System.currentTimeMillis()
                val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
                val path = File("${musicDir.absolutePath}/${timestamp}_video_converter.mp3")
                val cacheFile = File(outputPath)
                cacheFile.copyTo(path, overwrite = true)
                mp3Uri = Uri.parse(outputPath)
                val infoFile = FileInfo.getFileInfoFromPath(mp3Uri!!.toString())
                Const.videoConvert = VideoConvertModel(
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
            } else if(listOutputPath.size == listVideoPick.size){
                listOutputPath.forEachIndexed { index, cacheFile ->
                    Log.d("check_iust_out_put_path", "convertAudio: "+ index  + "  ____  "+ cacheFile)
                    val timestamp = System.currentTimeMillis()
                    val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
                    val path = File("${musicDir.absolutePath}/${timestamp}_convertMp3_$index.mp3")
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
                checkDone = true
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
        if (exoPlayer != null) {
            exoPlayer?.release() // Giải phóng tài nguyên
        }
        job?.cancel()

    }

    override fun onStop() {
        super.onStop()
        job?.cancel()
//        FFmpeg.cancel()
        if (exoPlayer?.isPlaying == true) {
            // Dừng phát âm thanh nếu đang phát
            exoPlayer?.pause() // Giải phóng tài nguyên
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
        return (mbSize * 1024 * 1024).toLong() // Nhân để chuyển đổi từ MB sang bytes
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        job?.cancel()
        FFmpeg.cancel()
        if (binding.loadingOverlay.visibility == View.VISIBLE) {
            hideLoadingOverlay()
            startCoroutine()

        } else {
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onResume() {
        super.onResume()
        listOutputPath.clear()
        listAudioSaved.clear()
        listAudioMerger.clear()
//        if(binding.loadingOverlay.visibility == View.VISIBLE){
//            hideLoadingOverlay()
//        }
//        if (binding.loadingOverlay.visibility == View.VISIBLE) {
//            if (listVideoPick.size == 1) {
//                val timestamp = System.currentTimeMillis()
////                val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
////                Log.d("check_path", "initAction: " + videoPath)
//                outputPath = "${cacheDir.path}/converter_to_mp3_cache_${timestamp}.mp3"
//                if (videoPath != null) {
//                    convertVideoToMp3(videoPath!!, outputPath)
//                }
//            } else {
//                job = CoroutineScope(Dispatchers.Main).launch {
//                    listAudioMerger.clear()
//                    listAudioSaved.clear()
//                    convertAllVideosToMp3()
//
//                }
//            }
//        }

    }

    fun startCoroutine() {
        listAudioSaved.clear()
        listAudioMerger.clear()
        listOutputPath.clear()
        isConverting = false
    }

}