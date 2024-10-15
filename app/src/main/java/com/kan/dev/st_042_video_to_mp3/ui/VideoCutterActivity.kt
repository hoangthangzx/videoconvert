package com.kan.dev.st_042_video_to_mp3.ui
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.arthenica.mobileffmpeg.FFmpeg
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivityVideoCutterBinding
import com.kan.dev.st_042_video_to_mp3.model.VideoCutterModel
import com.kan.dev.st_042_video_to_mp3.ui.saved.SavedActivity
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionVideoPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectType
import com.kan.dev.st_042_video_to_mp3.utils.FileInfo
import com.kan.dev.st_042_video_to_mp3.utils.applyGradient
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.AbsBaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class VideoCutterActivity : AbsBaseActivity<ActivityVideoCutterBinding>(false){
    override fun getFragmentID(): Int = 0
    override fun getLayoutId(): Int = R.layout.activity_video_cutter
    var videoUri: Uri? = null
    private var exoPlayer: ExoPlayer? = null
    var startTime = 0f
    var endTime = 0f
    var cutterUri = ""
    var checkCutter = false
    var maxValueTime = 0f
    var timeCut = ""

    private lateinit var frames: Array<ImageView>
    override fun init() {
        initData()
        initView()
        initAction()
    }
    private fun initView() {
        val colors = intArrayOf(
            ContextCompat.getColor(this@VideoCutterActivity, R.color.color_1),
            ContextCompat.getColor(this@VideoCutterActivity, R.color.color_2)
        )
        binding.tvDone.applyGradient(this@VideoCutterActivity,colors)
    }

    @OptIn(UnstableApi::class)
    private fun initAction() {
        binding.exoVideo.setOnClickListener{
            binding.playerControlView.show()
        }
        binding.playerControlView.setOnClickListener {
            binding.playerControlView.hide()
        }

        binding.imvBack.onSingleClick {
            finish()
        }
        binding.tvCancel.onSingleClick {
            finish()
        }
        binding.btnPlus.setOnClickListener {
            val cutPath = File(cutterUri)
            if (cutPath.exists()){
                cutPath.delete()
            }

            val currentTime = binding.edtStartTime.text.toString()
            val parts = currentTime.split(":")
            var hours = if (parts.size > 0) parts[0].toIntOrNull() ?: 0 else 0
            var minutes = if (parts.size > 1) parts[1].toIntOrNull() ?: 0 else 0
            Log.d("check_tiome", "initAction: "+ currentTime + " ==== "+ binding.edtEndTime.text.toString())
            if(convertTimeToSeconds(currentTime) == convertTimeToSeconds(binding.edtEndTime.text.toString()) - 1){
                binding.btnPlus.isClickable = false
            }else{
                checkCutter = true
                minutes += 1
                if (minutes >= 60) {
                    minutes = 0
                    hours += 1
                }
                if (hours > 23) {
                    hours = 0
                }
                startTime = convertDurationToSeconds(binding.edtStartTime.text.toString()).toFloat()
                binding.customRangeSeekBar.setSelectedMinValue(startTime + 1f)
//                if(binding.customRangeSeekBar.getSelectedMinValue() > 1f){
//                    binding.btnMinus.isClickable = true
//                }
//                if(binding.customRangeSeekBar.getSelectedMinValue() >= binding.customRangeSeekBar.getSelectedMaxValue() -1f){
//                    binding.btnPlus.isClickable = false
//                }
                binding.edtStartTime.setText(String.format("%02d:%02d", hours, minutes))
                binding.edtStartTime.setSelection(binding.edtStartTime.text.length)
                timeCut = convertSecondsToDuration(binding.customRangeSeekBar.getSelectedMaxValue().toInt() - binding.customRangeSeekBar.getSelectedMinValue().toInt())
                binding.tvTimeCut.text = timeCut
                val videoPath = getRealPathFromURI(this,videoUri!!)
                val currentValueStart = convertDurationToSeconds(binding.edtStartTime.text.toString())
                val currentValueEnd = convertDurationToSeconds(binding.edtEndTime.text.toString())
//                val currentValueEnd = convertDurationToSeconds(binding.tvTimeCut.text.toString())
                if (videoPath != null) {
                    cutVideo(videoPath,cutterUri,currentValueStart.toString(), currentValueEnd.toString())
                    Log.d("check_listener", "initAction: okeeeee")
                }
            }

        }
        binding.btnPlusEnd.setOnClickListener {
            val currentTime = binding.edtEndTime.text.toString()
            val parts = currentTime.split(":")
            var hours = if (parts.size > 0) parts[0].toIntOrNull() ?: 0 else 0
            var minutes = if (parts.size > 1) parts[1].toIntOrNull() ?: 0 else 0
            Log.d("check_max_value", "initAction: "+ convertTimeToSeconds(currentTime)  +"  ===== "    + binding.customRangeSeekBar.getMaxValue())
            if(convertTimeToSeconds(currentTime) >= binding.customRangeSeekBar.getMaxValue()){
                binding.btnPlusEnd.isClickable = false
                Toast.makeText(this, getString(R.string.time_reaches_its_maximum_value), Toast.LENGTH_SHORT).show()
            }else{
                binding.btnMinusEnd.isClickable = true
                minutes += 1
                if (minutes >= 60) {
                    minutes = 0
                    hours += 1
                }
                if (hours > 23) {
                    hours = 0
                }

                endTime = convertDurationToSeconds(binding.edtEndTime.text.toString()).toFloat()
                binding.customRangeSeekBar.setSelectedMaxValue(endTime + 1f)
                Log.d("check_value", "initActionMinus: "+ startTime)
                binding.edtEndTime.setText(String.format("%02d:%02d", hours, minutes))
                binding.edtEndTime.setSelection(binding.edtEndTime.text.length)
                timeCut = convertSecondsToDuration(binding.customRangeSeekBar.getSelectedMaxValue().toInt() - binding.customRangeSeekBar.getSelectedMinValue().toInt())
                binding.tvTimeCut.text = timeCut
            }

        }
        binding.btnMinusEnd.setOnClickListener {
            val currentTime = binding.edtEndTime.text.toString()
            val parts = currentTime.split(":")
            var hours = if (parts.size > 0) parts[0].toIntOrNull() ?: 0 else 0
            var minutes = if (parts.size > 1) parts[1].toIntOrNull() ?: 0 else 0
            if(convertTimeToSeconds(currentTime) == convertTimeToSeconds(binding.edtStartTime.text.toString()) + 1){
                binding.btnMinusEnd.isClickable = false
            }else{
                minutes -= 1
                if (minutes < 0) {
                    minutes = 59
                    hours -= 1
                }

                if (hours < 0) {
                    hours = 23
                }
                endTime = convertDurationToSeconds(binding.edtEndTime.text.toString()).toFloat()
                binding.customRangeSeekBar.setSelectedMaxValue(endTime - 1f)
                binding.edtEndTime.setText(String.format("%02d:%02d", hours, minutes))
//            if(binding.customRangeSeekBar.getSelectedMaxValue() < maxValueTime){
//                binding.btnPlusEnd.isClickable = true
//            }
//            if(binding.customRangeSeekBar.getSelectedMaxValue() <= binding.customRangeSeekBar.getSelectedMinValue() + 1f){
//                binding.btnMinusEnd.isClickable = false
//            }
                binding.edtEndTime.setSelection(binding.edtStartTime.text.length)
                timeCut = convertSecondsToDuration(binding.customRangeSeekBar.getSelectedMaxValue().toInt() - binding.customRangeSeekBar.getSelectedMinValue().toInt())
                binding.tvTimeCut.text = timeCut
            }

        }

        binding.btnMinus.setOnClickListener {
            val currentTime = binding.edtStartTime.text.toString()
            val parts = currentTime.split(":")
            var hours = if (parts.size > 0) parts[0].toIntOrNull() ?: 0 else 0
            var minutes = if (parts.size > 1) parts[1].toIntOrNull() ?: 0 else 0
            if(convertTimeToSeconds(currentTime) <= 0){
                binding.btnMinusEnd.isClickable = false
                Toast.makeText(this, getString(R.string.time_reaches_its_maximum_value), Toast.LENGTH_SHORT).show()
            }else{

                binding.btnPlus.isClickable = true
                minutes -= 1
                if (minutes < 0) {
                    minutes = 59
                    hours -= 1
                }
                if (hours < 0) {
                    hours = 23
                }
                startTime = convertDurationToSeconds(binding.edtStartTime.text.toString()).toFloat()
                binding.customRangeSeekBar.setSelectedMinValue(startTime - 1f)
//                if(binding.customRangeSeekBar.getSelectedMinValue() < 1f){
//                    binding.btnMinus.isClickable = false
//                    Toast.makeText(this, getString(R.string.time_to_reach_minimum_value), Toast.LENGTH_SHORT).show()
//                }
//                if(binding.customRangeSeekBar.getSelectedMinValue() < binding.customRangeSeekBar.getSelectedMaxValue() ){
//                    binding.btnPlus.isClickable = true
//                }
                binding.edtStartTime.setText(String.format("%02d:%02d", hours, minutes))
                binding.edtStartTime.setSelection(binding.edtStartTime.text.length)
                timeCut = convertSecondsToDuration(binding.customRangeSeekBar.getSelectedMaxValue().toInt() - binding.customRangeSeekBar.getSelectedMinValue().toInt())
                binding.tvTimeCut.text = timeCut
            }

        }

//        binding.edtStartTime.isClickable = false
//        binding.edtEndTime.isClickable = false
        binding.customRangeSeekBar.setOnRangeSeekBarChangeListener(object : CustomRangeSeekBar.OnRangeSeekBarChangeListener {
            override fun onRangeSeekBarValuesChanged(minValue: Float, maxValue: Float) {
                startTime = minValue
                endTime = maxValue
                timeCut = convertSecondsToDuration(endTime.toInt() - startTime.toInt())
                binding.btnPlusEnd.isClickable = true
                binding.btnMinusEnd.isClickable = true
                binding.btnPlus.isClickable = true
                binding.btnMinus.isClickable = true
                binding.tvTimeCut.text = timeCut
                binding.edtStartTime.setText(convertSecondsToDuration(startTime.toInt()))
                binding.edtEndTime.setText(convertSecondsToDuration(endTime.toInt()))
                Log.d("check_value", "onRangeSeekBarValuesChanged: "+ minValue + "_________" + maxValue)
            }
        })
        Log.d("check_duration", "initAction: "+ listVideo[positionVideoPlay].duration)
        binding.tvDone.onSingleClick {
            val currentValueStart = convertDurationToSeconds(binding.edtStartTime.text.toString())
            val currentValueEnd = convertDurationToSeconds(binding.tvTimeCut.text.toString())

            val durationVideo = convertDurationToSeconds(listVideo[positionVideoPlay].duration)
            if(currentValueStart > currentValueEnd || currentValueStart > durationVideo || currentValueEnd > durationVideo || currentValueEnd <0 || currentValueStart < 0 ){
                Toast.makeText(this@VideoCutterActivity, getString(R.string.you_must_choose_the_right_time), Toast.LENGTH_SHORT).show()
            }else{
                showLoadingOverlay()
                val videoPath = getRealPathFromURI(this,videoUri!!)
                val timestamp = System.currentTimeMillis()
                val musicDir = File(Environment.getExternalStorageDirectory(), "Movies/video")
                val outputPath = "${musicDir.absolutePath}/${File(videoPath).name.substringBeforeLast(".") }_${timestamp}_cutter.mp4"
                if (videoPath != null) {
                    cutVideo(videoPath,outputPath,formatTime_1(currentValueStart.toInt()),formatTime_1(currentValueEnd.toInt()))
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun initData() {
        cutterUri = "${cacheDir.path}/video_cutter_00.mp4"
        maxValueTime = (convertDurationToSeconds(listVideo[positionVideoPlay].duration).toFloat())
        startTime = (convertDurationToSeconds(listVideo[positionVideoPlay].duration).toFloat())*(1f/3f)
        endTime =  (convertDurationToSeconds(listVideo[positionVideoPlay].duration).toFloat())*(2f/3f)
        binding.customRangeSeekBar.setSelectedMinValue(startTime)
        binding.customRangeSeekBar.setSelectedMaxValue(endTime)
        binding.customRangeSeekBar.setMaxValue(convertDurationToSeconds(listVideo[positionVideoPlay].duration))
        timeCut = convertSecondsToDuration(endTime.toInt() - startTime.toInt())
        binding.tvTimeCut.text = timeCut
        binding.edtStartTime.setText(formatTime(startTime.toInt()))
        binding.edtEndTime.setText(formatTime(endTime.toInt()))
        if (listVideo.size > 0) {
            videoUri = Uri.parse(listVideo[positionVideoPlay].uri.toString())
        }
//        exoPlayer = ExoPlayer.Builder(this).build()
//        binding.exoVideo.player = exoPlayer
//        binding.playerControlView.player = exoPlayer
//        binding.playerControlView.showTimeoutMs = 3000
//
//        val mediaItem = MediaItem.fromUri(videoUri!!)
//        exoPlayer!!.setMediaItem(mediaItem)
//        exoPlayer!!.prepare()
////        exoPlayer!!.play()
//        exoPlayer!!.playWhenReady = true
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            binding.exoVideo.player = this
            binding.playerControlView.player = this
            binding.playerControlView.showTimeoutMs = 3000
            val mediaItem = MediaItem.fromUri(videoUri!!)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = false// Bắt đầu phát video
//            exoPlayer!!.play()
        }

        frames = arrayOf(
            findViewById(R.id.imvFrame1),
            findViewById(R.id.imvFrame2),
            findViewById(R.id.imvFrame3),
            findViewById(R.id.imvFrame4),
            findViewById(R.id.imvFrame5),
            findViewById(R.id.imvFrame6),
            findViewById(R.id.imvFrame7),
            findViewById(R.id.imvFrame8),
        )
//        val retriever = MediaMetadataRetriever()
//        retriever.setDataSource(this, videoUri)
//        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
//        val numberOfFrames = 8
//        val interval = duration / numberOfFrames
//        for (i in 0 until numberOfFrames) {
//            val frame: Bitmap? = retriever.getFrameAtTime(i * interval * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
//            if (frame != null) {
//                frames[i].setImageBitmap(frame)
//            }
//        }
//        retriever.release()
        GlobalScope.launch(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()
            try {
                // Thiết lập nguồn video
                retriever.setDataSource(this@VideoCutterActivity, videoUri)
                val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
                val numberOfFrames = 8
                val interval = duration / numberOfFrames

                // Lấy từng khung hình và hiển thị
                for (i in 0 until numberOfFrames) {
                    val frame: Bitmap? = retriever.getFrameAtTime(i * interval * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    // Chuyển về luồng chính để cập nhật giao diện người dùng
                    withContext(Dispatchers.Main) {
                        if (frame != null) {
                            frames[i].setImageBitmap(frame) // Cập nhật từng ImageView
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                retriever.release() // Giải phóng tài nguyên
            }
        }
    }


    private fun showLoadingOverlay() {
        val animator = ObjectAnimator.ofInt(binding.lottieAnimationView, "progress", 0, 100)
        animator.duration = 1000 // Thời gian chạy animation (5 giây)
        animator.start()
        binding.loadingOverlay.visibility = View.VISIBLE
    }

    private fun hideLoadingOverlay() {
        binding.loadingOverlay.visibility = View.GONE
    }

    @OptIn(UnstableApi::class)
    fun cutVideo(inputFilePath: String, outputFilePath: String, startTime: String, endTime: String) {
        Log.d("check_audio_speed", ""+ startTime + "    " + endTime)
        val command = "-i \"$inputFilePath\" -ss \"$startTime\" -c copy -t \"$endTime\" \"$outputFilePath\""
        Log.d("check_audio_speed", ""+ startTime + "    " + endTime + "     " + command)
        val resultCode = FFmpeg.execute(command)
        if (resultCode == 0) {
            Log.d("check_listener", "initAction: cut thanh cong")
            if(checkCutter == true){
                Log.d("check_listener", "initAction: videoeoeoeoe")
                checkCutter = false
                exoPlayer = ExoPlayer.Builder(this).build().apply {
                    binding.exoVideo.player = this
                    binding.playerControlView.player = this
                    binding.playerControlView.showTimeoutMs = 3000
                    val mediaItem = MediaItem.fromUri(cutterUri)
                    setMediaItem(mediaItem)
                    prepare()
                    playWhenReady = false
                }
            }else{
                videoUri = Uri.parse(outputFilePath)
                val infoFile = FileInfo.getFileInfoFromPath(videoUri!!.toString())
                Const.videoCutter = VideoCutterModel(videoUri!!,timeCut,infoFile!!.fileSize,infoFile.fileName.toString() )
                startActivity(Intent(this@VideoCutterActivity, SavedActivity::class.java))
            }
            Log.d("check_audio_speed", "Thay đổi tốc độ âm thanh cong" + videoUri)
        } else {
            Log.d("check_audio_speed", "Thay đổi tốc độ âm thanh thất bại. Mã lỗi: $resultCode")
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

    fun convertDurationToSeconds(duration: String): Int {
        val parts = duration.split(":").map { it.toInt() }
        return parts.fold(0) { acc, part -> acc * 60 + part }
    }

    fun convertSecondsToDuration(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    @SuppressLint("DefaultLocale")
    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    fun formatTime_1(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
    }

    fun convertTimeToSeconds(time: String): Int {
        val timeParts = time.split(":")  // Tách chuỗi thành phút và giây
        val minutes = timeParts[0].toInt()  // Chuyển đổi phần phút thành số nguyên
        val seconds = timeParts[1].toInt()  // Chuyển đổi phần giây thành số nguyên

        return minutes * 60 + seconds  // Tính tổng số giây
    }

    override fun onResume() {
        super.onResume()
        initData()
        selectType = "VideoCutter"
        Log.d("check_type_fr", "onResume: " + selectType)
//        initData()
    }
    override fun onStop() {
        super.onStop()
        exoPlayer?.release()
        hideLoadingOverlay()
    }

}