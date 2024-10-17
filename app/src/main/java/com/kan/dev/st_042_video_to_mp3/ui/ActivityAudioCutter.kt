package com.kan.dev.st_042_video_to_mp3.ui

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.arthenica.mobileffmpeg.FFmpeg
import com.google.android.material.tabs.TabLayout
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivityAudioCutterBinding
import com.kan.dev.st_042_video_to_mp3.model.VideoCutterModel
import com.kan.dev.st_042_video_to_mp3.ui.saved.SavedActivity
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionAudioPlay
import com.kan.dev.st_042_video_to_mp3.utils.FileInfo
import com.kan.dev.st_042_video_to_mp3.utils.applyGradient
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.AbsBaseActivity
import kotlinx.coroutines.Job
import java.io.File
import java.io.IOException

class ActivityAudioCutter : AbsBaseActivity<ActivityAudioCutterBinding>(false) {
    override fun getFragmentID(): Int  = 0
    override fun getLayoutId(): Int = R.layout.activity_audio_cutter
    private var isPlaying: Boolean = false
    var mediaPlayer: MediaPlayer? = null
    private var handler = android.os.Handler()
    var startTime = 0f
    var endTime = 0f
    var maxValueTime = 0f
    var checkType = true
    var checkCut = true
    var timeCut = ""
    var timeSum = 0f
    private var job: Job? = null
    var cutterUri = ""
    override fun init() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Trim sides"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Trim middle"))
        initView()
        initData()
        initAction()
    }

    private fun initView() {
        val colors = intArrayOf(
            ContextCompat.getColor(this@ActivityAudioCutter, R.color.color_1),
            ContextCompat.getColor(this@ActivityAudioCutter, R.color.color_2)
        )
        binding.tvDone.applyGradient(this@ActivityAudioCutter,colors)
    }

    private fun initAction() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        timeCut = convertSecondsToDuration(endTime.toInt() - startTime.toInt())
                        binding.tvTimeCut.text = timeCut
                        binding.customCutterSeekBar.resetBackgroundColorToDefault()
                        checkType = true
                        if(checkCut == false){
                            mediaPlayer?.pause()
                            mediaPlayer?.seekTo(0)
                        }
                        checkCut = true
                        isPlaying = false
                        binding.imvPlay.visibility = View.VISIBLE
                        binding.imvPause.visibility = View.GONE
                    }
                    1 -> {
                        timeCut = convertSecondsToDuration(startTime.toInt() + timeSum.toInt()- endTime.toInt())
                        binding.tvTimeCut.text = timeCut
                        binding.customCutterSeekBar.changeBackgroundColor()
                        checkType = false
                        isPlaying = false
                        if(checkCut == false){
                            mediaPlayer?.pause()
                            mediaPlayer?.seekTo(0)
                        }
                        checkCut = true
                        binding.imvPlay.visibility = View.VISIBLE
                        binding.imvPause.visibility = View.GONE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        binding.imvBack.onSingleClick {
            finish()
        }
        binding.tvCancel.onSingleClick {
            finish()
        }
        binding.btnPlus.setOnClickListener {
            val currentTime = binding.edtStartTime.text.toString()
            binding.btnMinus.isClickable = true
            if(convertTimeToSeconds(currentTime) == convertTimeToSeconds(binding.edtEndTime.text.toString()) - 1){
                binding.btnPlus.isClickable = false
            }else{
                val parts = currentTime.split(":")
                var hours = if (parts.size > 0) parts[0].toIntOrNull() ?: 0 else 0
                var minutes = if (parts.size > 1) parts[1].toIntOrNull() ?: 0 else 0
                minutes += 1
                if (minutes >= 60) {
                    minutes = 0
                    hours += 1
                }
                if (hours > 23) {
                    hours = 0
                }
                isPlaying = false
                if (checkCut == false){
                    mediaPlayer!!.release()
                }
                checkCut = true
                binding.imvPlay.visibility = View.VISIBLE
                binding.imvPause.visibility = View.GONE
                startTime = convertDurationToSeconds(binding.edtStartTime.text.toString()).toFloat()
                binding.customCutterSeekBar.setSelectedMinValue(startTime + 1f)
                binding.edtStartTime.setText(String.format("%02d:%02d", hours, minutes))
                binding.edtStartTime.setSelection(binding.edtStartTime.text.length)
                if(checkType== true){
                    timeCut = convertSecondsToDuration(binding.customCutterSeekBar.getSelectedMaxValue().toInt() - binding.customCutterSeekBar.getSelectedMinValue().toInt())
                }else{
                    timeCut = convertSecondsToDuration(  timeSum.toInt() - binding.customCutterSeekBar.getSelectedMaxValue().toInt() + binding.customCutterSeekBar.getSelectedMinValue().toInt())
                }
                binding.tvTimeCut.text = timeCut
            }
        }

        binding.btnPlusEnd.setOnClickListener {
            val currentTime = binding.edtEndTime.text.toString()
            binding.btnMinusEnd.isClickable = true
            val parts = currentTime.split(":")
            var hours = if (parts.size > 0) parts[0].toIntOrNull() ?: 0 else 0
            var minutes = if (parts.size > 1) parts[1].toIntOrNull() ?: 0 else 0
            if(convertTimeToSeconds(currentTime) >= binding.customCutterSeekBar.getMaxValue()){
                binding.btnPlusEnd.isClickable = false
                Toast.makeText(this, getString(R.string.time_reaches_its_maximum_value), Toast.LENGTH_SHORT).show()
            }else{
                minutes += 1
                if (minutes >= 60) {
                    minutes = 0
                    hours += 1
                }
                if (hours > 23) {
                    hours = 0
                }
                isPlaying = false
                if ( checkCut == false){
                    mediaPlayer!!.release()
                }
                checkCut = true
                binding.imvPlay.visibility = View.VISIBLE
                binding.imvPause.visibility = View.GONE
                endTime = convertDurationToSeconds(binding.edtEndTime.text.toString()).toFloat()
                binding.customCutterSeekBar.setSelectedMaxValue(endTime + 1f)
                Log.d("check_value", "initActionMinus: "+ startTime)
                binding.edtEndTime.setText(String.format("%02d:%02d", hours, minutes))
                binding.edtEndTime.setSelection(binding.edtEndTime.text.length)
                if(checkType== true){
                    timeCut = convertSecondsToDuration(binding.customCutterSeekBar.getSelectedMaxValue().toInt() - binding.customCutterSeekBar.getSelectedMinValue().toInt())
                }else{
                    timeCut = convertSecondsToDuration(  timeSum.toInt() - binding.customCutterSeekBar.getSelectedMaxValue().toInt() + binding.customCutterSeekBar.getSelectedMinValue().toInt())
                }
                binding.tvTimeCut.text = timeCut
            }
        }

        binding.btnMinusEnd.setOnClickListener {
            val currentTime = binding.edtEndTime.text.toString()
            binding.btnPlusEnd.isClickable = true
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
                isPlaying = false
                if (checkCut == false){
                    mediaPlayer!!.release()
                }
                checkCut = true
                binding.imvPlay.visibility = View.VISIBLE
                binding.imvPause.visibility = View.GONE
                endTime = convertDurationToSeconds(binding.edtEndTime.text.toString()).toFloat()
                binding.customCutterSeekBar.setSelectedMaxValue(endTime - 1f)
                binding.edtEndTime.setText(String.format("%02d:%02d", hours, minutes))
                binding.edtEndTime.setSelection(binding.edtStartTime.text.length)
                if(checkType== true){
                    timeCut = convertSecondsToDuration(binding.customCutterSeekBar.getSelectedMaxValue().toInt() - binding.customCutterSeekBar.getSelectedMinValue().toInt())
                }else{
                    timeCut = convertSecondsToDuration(  timeSum.toInt() - binding.customCutterSeekBar.getSelectedMaxValue().toInt() + binding.customCutterSeekBar.getSelectedMinValue().toInt())
                }

                binding.tvTimeCut.text = timeCut
            }

        }

        binding.btnMinus.setOnClickListener {
            val currentTime = binding.edtStartTime.text.toString()
            binding.btnPlus.isClickable = true
            val parts = currentTime.split(":")
            var hours = if (parts.size > 0) parts[0].toIntOrNull() ?: 0 else 0
            var minutes = if (parts.size > 1) parts[1].toIntOrNull() ?: 0 else 0
            if(convertTimeToSeconds(currentTime) <= 0){
                binding.btnMinus.isClickable = false
                Toast.makeText(this, getString(R.string.time_to_reach_minimum_value), Toast.LENGTH_SHORT).show()
            }else{
                minutes -= 1
                if (minutes < 0) {
                    minutes = 59
                    hours -= 1
                }

                if (hours < 0) {
                    hours = 23
                }
                isPlaying = false
                if (checkCut == false){
                    mediaPlayer!!.release()
                }
                checkCut = true
                binding.imvPlay.visibility = View.VISIBLE
                binding.imvPause.visibility = View.GONE
                startTime = convertDurationToSeconds(binding.edtStartTime.text.toString()).toFloat()
                binding.customCutterSeekBar.setSelectedMinValue(startTime - 1f)
                binding.edtStartTime.setText(String.format("%02d:%02d", hours, minutes))
                binding.edtStartTime.setSelection(binding.edtStartTime.text.length)
                if(checkType== true){
                    timeCut = convertSecondsToDuration(binding.customCutterSeekBar.getSelectedMaxValue().toInt() - binding.customCutterSeekBar.getSelectedMinValue().toInt())
                }else{
                    timeCut = convertSecondsToDuration(  timeSum.toInt() - binding.customCutterSeekBar.getSelectedMaxValue().toInt() + binding.customCutterSeekBar.getSelectedMinValue().toInt())
                }
                binding.tvTimeCut.text = timeCut
            }
        }
        binding.customCutterSeekBar.setOnRangeSeekBarChangeListener(object : CustomCutterSeekBar.OnRangeSeekBarChangeListener {
            override fun onRangeSeekBarValuesChanged(minValue: Float, maxValue: Float) {
                startTime = minValue
                endTime = maxValue
                if (checkCut == false){
                    mediaPlayer!!.stop()
                    mediaPlayer!!.seekTo(0)
                }
                checkCut = true
                isPlaying = false
                binding.imvPlay.visibility = View.VISIBLE
                binding.imvPause.visibility = View.GONE
                binding.btnPlusEnd.isClickable = true
                binding.btnMinusEnd.isClickable = true
                binding.btnPlus.isClickable = true
                binding.btnMinus.isClickable = true
                if(checkType== true){
                    timeCut = convertSecondsToDuration(binding.customCutterSeekBar.getSelectedMaxValue().toInt() - binding.customCutterSeekBar.getSelectedMinValue().toInt())
                }else{
                    timeCut = convertSecondsToDuration(  timeSum.toInt() - binding.customCutterSeekBar.getSelectedMaxValue().toInt() + binding.customCutterSeekBar.getSelectedMinValue().toInt())
                }
                binding.tvTimeCut.text = timeCut
                binding.edtStartTime.setText(convertSecondsToDuration(startTime.toInt()))
                binding.edtEndTime.setText(convertSecondsToDuration(endTime.toInt()))
                Log.d("check_value", "onRangeSeekBarValuesChanged: "+ minValue + "_________" + maxValue)
            }
        })

        binding.imv15Left.setOnClickListener {
            rewindAudio(15000)
        }
        binding.imv15Right.setOnClickListener {
            forwardAudio(15000)
        }

        binding.imvPlay.onSingleClick {
            if(checkCut == true){
//                binding.progress.visibility = View.VISIBLE
                val currentValueStart = convertDurationToSeconds(binding.edtStartTime.text.toString())
                val currentValueEnd = convertDurationToSeconds(binding.edtEndTime.text.toString())
//                val durationVideo = convertDurationToSeconds(listAudio[positionAudioPlay].duration)
                val timestamp = System.currentTimeMillis()
                cutterUri = "${cacheDir.path}/cutter_${timestamp}.mp3"
                Log.d("check_click", "initAction: thanhhhh anjanah"+ currentValueStart + "    " + currentValueEnd)
                val videoPath = getRealPathFromURI(this, listAudio[positionAudioPlay].uri)
                if (videoPath != null) {
                    if(checkType == true){
                        cutAudio(videoPath,cutterUri,formatTime(currentValueStart.toInt()),formatTime(currentValueEnd.toInt()))
                    }else{
                        cutAndMergeAudio(videoPath,cutterUri,formatTime(currentValueStart.toInt()),formatTime(currentValueEnd.toInt()))
                    }
                }
                binding.imvPause.visibility = View.VISIBLE
                binding.imvPlay.visibility = View.GONE
//                createMediaPlayer()
//                mediaPlayer?.setOnCompletionListener {
//                    mediaPlayer!!.seekTo(0)
//                    binding.imvPause.visibility = View.GONE
//                    binding.imvPlay.visibility = View.VISIBLE
//                }
            }else{
                Log.d("check_click", "initAction: huefuyhehgehghwttghtanah")
                binding.imvPause.visibility = View.VISIBLE
                binding.imvPlay.visibility = View.GONE
                startPlaying()
            }

        }

        binding.imvPause.onSingleClick {
            binding.imvPause.visibility = View.GONE
            binding.imvPlay.visibility = View.VISIBLE
            pausePlaying()
        }

        binding.tvDone.onSingleClick {
            checkCut = false
            if(mediaPlayer!!.isPlaying){
                mediaPlayer?.stop()
            }
            binding.imvPause.visibility = View.GONE
            binding.imvPlay.visibility = View.VISIBLE
            val currentValueStart = convertDurationToSeconds(binding.edtStartTime.text.toString())
            val currentValueEnd = convertDurationToSeconds(binding.edtEndTime.text.toString())
            val durationVideo = convertDurationToSeconds(listAudio[positionAudioPlay].duration)
            if(currentValueStart > currentValueEnd || currentValueStart > durationVideo || currentValueEnd > durationVideo || currentValueEnd <0 || currentValueStart < 0 ){
                Toast.makeText(this@ActivityAudioCutter, getString(R.string.you_must_choose_the_right_time), Toast.LENGTH_SHORT).show()
            }else{
                showLoadingOverlay()
                val videoPath = getRealPathFromURI(this, listAudio[positionAudioPlay].uri)
                val timestamp = System.currentTimeMillis()
                val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
                Log.d("checkType", "initAction: "+ checkType+ "   "  + videoPath)
                val outputPath = "${musicDir.absolutePath}/${File(videoPath).name.substringBeforeLast(".") }_${timestamp}_cutter.mp3"
                if (videoPath != null) {
                    if (checkType == true){
                        cutAudio(videoPath,outputPath,formatTime(currentValueStart.toInt()),formatTime(currentValueEnd.toInt()))
                    }else{
                        cutAndMergeAudio(videoPath,outputPath,formatTime(currentValueStart.toInt()),formatTime(currentValueEnd.toInt()))
                    }
                }
            }
        }
    }

    private fun rewindAudio(milliseconds: Int) {
        mediaPlayer?.let {
            val newPosition = it.currentPosition - milliseconds
            if (newPosition >= 0) {
                Log.d("check_new_pos", "rewindAudio: "+ newPosition)
                it.seekTo(newPosition)
            } else {
                it.seekTo(0)
            }
        }
    }

    private fun forwardAudio(milliseconds: Int) {
        mediaPlayer?.let {
            val newPosition = it.currentPosition + milliseconds
            if (newPosition <= it.duration) {
                it.seekTo(newPosition)
            } else {
                it.seekTo(it.duration)
                checkCut = true
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initData() {
        mediaPlayer = MediaPlayer()
        binding.waveformSeekBar.setSampleFrom(listAudio[positionAudioPlay].uri)
        maxValueTime = (convertDurationToSeconds(listAudio[positionAudioPlay].duration).toFloat())
        startTime = (convertDurationToSeconds(listAudio[positionAudioPlay].duration).toFloat())*(1f/3f)
        endTime =  (convertDurationToSeconds(listAudio[positionAudioPlay].duration).toFloat())*(2f/3f)
        binding.customCutterSeekBar.setSelectedMinValue(startTime)
        timeSum = (convertDurationToSeconds(listAudio[positionAudioPlay].duration).toFloat())
        binding.customCutterSeekBar.setSelectedMaxValue(endTime)
        binding.customCutterSeekBar.setMaxValue(convertDurationToSeconds(listAudio[positionAudioPlay].duration))
        if(checkType == true){
            timeCut = convertSecondsToDuration(endTime.toInt() - startTime.toInt())
        }else{
            timeCut = convertSecondsToDuration(startTime.toInt() + timeSum.toInt()- endTime.toInt())
        }
        binding.tvTimeCut.text = timeCut
        binding.edtStartTime.setText(formatTime(startTime.toInt()))
        binding.edtEndTime.setText(formatTime(endTime.toInt()))
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

    private fun createMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(cutterUri) // Không cần ?. vì đã kiểm tra tồn tại
                prepare()
                Log.d("AudioPlay", "Playback started")
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("AudioPlay", "Failed to prepare media player: ${e.message}")
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


    private fun startPlaying() {
        if (!isPlaying) {
            mediaPlayer?.start()
            isPlaying = true
        }
    }

    private fun pausePlaying() {
        if (isPlaying) {
            mediaPlayer?.pause()
            isPlaying = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        job?.cancel()
        hideLoadingOverlay()
    }

    fun cutAndMergeAudio(inputFilePath: String, outputFilePath: String, startTime: String, endTime: String) {
        // Cắt đoạn từ đầu đến startTime
        val firstPartPath = "${cacheDir.path}/first_part.mp3"
        val firstPartCommand = "-i \"$inputFilePath\" -to $startTime -c copy $firstPartPath"
        val firstPartResult = FFmpeg.execute(firstPartCommand)
        // Cắt đoạn từ endTime đến cuối file
        val secondPartPath = "${cacheDir.path}/second_part.mp3"
        val secondPartCommand = "-i \"$inputFilePath\" -ss $endTime -c copy $secondPartPath"
        val secondPartResult = FFmpeg.execute(secondPartCommand)

        if (firstPartResult == 0 && secondPartResult == 0) {
            // Ghép hai đoạn lại
            val mergeCommand = "-i \"concat:$firstPartPath|$secondPartPath\" -c copy \"$outputFilePath\""
            val mergeResult = FFmpeg.execute(mergeCommand)

            if (mergeResult == 0) {
                val videoUri = Uri.parse(outputFilePath)
                val fPath = File(firstPartPath)
                fPath.delete()
                val sPath = File(secondPartPath)
                sPath.delete()
                if(checkCut == true){
                    Log.d("check_click", "cutAudio: "+ cutterUri)
                    binding.progress.visibility = View.GONE
                    createMediaPlayer()
                    startPlaying()
                    mediaPlayer?.setOnCompletionListener {
                        binding.imvPlay.visibility = View.VISIBLE
                        binding.imvPause.visibility = View.GONE
                        isPlaying = false
                    }
//                    val cutPath = File(cutterUri)
//                    cutPath.delete()
                    checkCut = false
                }else{
                    val videoUri = Uri.parse(outputFilePath)
                    val infoFile = FileInfo.getFileInfoFromPath(videoUri.toString())
                    Const.audioCutter = VideoCutterModel(videoUri,timeCut ,infoFile!!.fileSize, infoFile.fileName.toString())
                    startActivity(Intent(this@ActivityAudioCutter, SavedActivity::class.java))
                }
//                val infoFile = FileInfo.getFileInfoFromPath(videoUri.toString())
//                Const.audioCutter = VideoCutterModel(videoUri,timeCut ,infoFile!!.fileSize, infoFile.fileName.toString())
//                startActivity(Intent(this@ActivityAudioCutter, SavedActivity::class.java))
                Log.d("check_audio_mmmm", "Cắt và ghép âm thanh thành công: $videoUri")
            } else {
                Log.d("check_audio_mmmm", "Ghép âm thanh thất bại. Mã lỗi: $mergeResult")
            }
        } else {
            Log.d("check_audio_mmmm", "Cắt âm thanh thất bại.")
        }
    }

    fun cutAudio(inputFilePath: String, outputFilePath: String, startTime: String, endTime: String) {
        Log.d("check_audio_speed", "$startTime    $endTime")
        val command = "-i \"$inputFilePath\" -ss $startTime -to $endTime -c copy \"$outputFilePath\""
        val resultCode = FFmpeg.execute(command)
        if (resultCode == 0) {
            Log.d("checkType", "cutAudio: "+ checkCut)
            if(checkCut == true){
                Log.d("check_click", "cutAudio: "+ cutterUri + "    " + isPlaying)
                binding.progress.visibility = View.GONE
                createMediaPlayer()
                startPlaying()
                mediaPlayer?.setOnCompletionListener {
                    binding.imvPlay.visibility = View.VISIBLE
                    binding.imvPause.visibility = View.GONE
                    isPlaying = false
                }
//                val cutPath = File(cutterUri)
//                cutPath.delete()
                checkCut = false
            }else{
                val videoUri = Uri.parse(outputFilePath)
                val infoFile = FileInfo.getFileInfoFromPath(videoUri.toString())
                Const.audioCutter = VideoCutterModel(videoUri,timeCut ,infoFile!!.fileSize, infoFile.fileName.toString())
                startActivity(Intent(this@ActivityAudioCutter, SavedActivity::class.java))
            }
        } else {
            Log.d("check_audio_speed", "Cắt âm thanh thất bại. Mã lỗi: $resultCode")
        }
    }


    fun convertTimeToSeconds(time: String): Int {
        val timeParts = time.split(":")  // Tách chuỗi thành phút và giây
        val minutes = timeParts[0].toInt()  // Chuyển đổi phần phút thành số nguyên
        val seconds = timeParts[1].toInt()  // Chuyển đổi phần giây thành số nguyên

        return minutes * 60 + seconds  // Tính tổng số giây
    }

    fun convertDurationToSeconds(duration: String): Int {
        val parts = duration.split(":").map { it.toInt() }
        return parts.fold(0) { acc, part -> acc * 60 + part }
    }

    override fun onResume() {
        super.onResume()
        if (mediaPlayer != null){
            mediaPlayer!!.start()
        }
        checkCut = true
    }

    override fun onStop() {
        super.onStop()
        if (mediaPlayer!=null && mediaPlayer!!.isPlaying){
            mediaPlayer!!.pause()
        }
        binding.imvPause.visibility = View.VISIBLE
        binding.imvPlay.visibility = View.GONE
    }


    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        job?.cancel()
        if(binding.loadingOverlay.visibility == View.VISIBLE){
            hideLoadingOverlay()
        }else{
            finish()
        }
    }
}