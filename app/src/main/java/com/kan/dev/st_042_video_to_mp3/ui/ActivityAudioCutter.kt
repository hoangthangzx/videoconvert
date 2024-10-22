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
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.arthenica.mobileffmpeg.FFmpeg
import com.google.android.material.tabs.TabLayout
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivityAudioCutterBinding
import com.kan.dev.st_042_video_to_mp3.model.VideoCutterModel
import com.kan.dev.st_042_video_to_mp3.ui.saved.SavedActivity
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.Const.countPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionAudioPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectTypeAudio
import com.kan.dev.st_042_video_to_mp3.utils.FileInfo
import com.kan.dev.st_042_video_to_mp3.utils.FileInfo.formatDuration
import com.kan.dev.st_042_video_to_mp3.utils.VideoUtils.formatTimeToHoursMinutes_1
import com.kan.dev.st_042_video_to_mp3.utils.applyGradient
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.AbsBaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

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
        binding.seekBarAudio.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("DefaultLocale")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer!!.seekTo(progress)
                    val elapsedTime = String.format(
                        "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(progress.toLong()),
                        TimeUnit.MILLISECONDS.toSeconds(progress.toLong()) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(progress.toLong()))
                    )
                    binding.tvTimeStart.text = "${elapsedTime}"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacksAndMessages(null)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                updateTimeAndSeekBar()
            }
        })

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
//                if (checkCut == false){
//                    mediaPlayer!!.release()
//                }
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
//                if ( checkCut == false){
//                    mediaPlayer!!.release()
//                }
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
//                if (checkCut == false){
//                    mediaPlayer!!.release()
//                }
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
//                if (checkCut == false){
//                    mediaPlayer!!.release()
//                }
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
                    mediaPlayer!!.pause()
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
            countPlay += 1
            Log.d("check_play", "initAction: "+ checkCut)
            if(checkCut == true){
                val currentValueStart = convertDurationToSeconds(binding.edtStartTime.text.toString())
                val currentValueEnd = convertDurationToSeconds(binding.edtEndTime.text.toString())
                val timestamp = System.currentTimeMillis()
                cutterUri = "${cacheDir.path}/cutter_${timestamp}.mp3"
                Log.d("check_click", "initAction: thanhhhh anjanah"+ currentValueStart + "    " + currentValueEnd)
                val videoPath = getRealPathFromURI(this, listAudio[positionAudioPlay].uri)
                if (videoPath != null) {
                    if(checkType == true){
                        checkCut = true
                        cutAudio(videoPath,cutterUri,formatTime(currentValueStart.toInt()),formatTime(currentValueEnd.toInt()))
                    }else{
                        checkCut = true
                        cutAndMergeAudio(videoPath,cutterUri,formatTime(currentValueStart.toInt()),formatTime(currentValueEnd.toInt()))
                    }
                }
                binding.seekBarAudio.isEnabled = true
                binding.progress.visibility = View.GONE
                createMediaPlayer()
                Log.d("check_audio_speed", "fvgrnrohegjegmbvkermkbhtrnh")
                checkCut = false
                binding.progress.visibility = View.GONE
                binding.seekBarAudio.max = mediaPlayer!!.duration
                binding.tvDuration.text = "/ ${formatDuration(mediaPlayer!!.duration)}"
                updateTimeAndSeekBar()
                val handler = Handler(Looper.getMainLooper())
                binding.imvPause.visibility = View.VISIBLE
                binding.imvPlay.visibility = View.GONE
                mediaPlayer!!.setOnCompletionListener {
                    binding.tvTimeStart.text =
                        formatTimeToHoursMinutes_1(mediaPlayer!!.duration)
                        handler.postDelayed({
                        binding.seekBarAudio.progress = 0
                        mediaPlayer!!.seekTo(0)
                        binding.tvTimeStart.text = "00:00"
                        isPlaying = false
                        binding.imvPause.visibility = View.GONE
                        binding.imvPlay.visibility = View.VISIBLE
//                                    handler.removeCallbacksAndMessages(null)
                    }, 1000)
                }
                binding.imvPause.visibility = View.VISIBLE
                binding.imvPlay.visibility = View.GONE
                startPlaying()
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
            if(mediaPlayer!!.isPlaying){
                mediaPlayer?.pause()
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
                        checkCut = false
                        cutAudio(videoPath,outputPath,formatTime(currentValueStart.toInt()),formatTime(currentValueEnd.toInt()))
                    }else{
                        checkCut = false
                        cutAndMergeAudio(videoPath,outputPath,formatTime(currentValueStart.toInt()),formatTime(currentValueEnd.toInt()))
                    }
                }
            }
        }
    }

    fun formatDuration(duration: Int): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    @SuppressLint("DefaultLocale")
    private fun updateTimeAndSeekBar() {
        val mediaPlayer = mediaPlayer ?: return
        if (mediaPlayer!=null){
            val currentPosition = mediaPlayer.currentPosition
            binding.seekBarAudio.progress = currentPosition
            val elapsedTime = String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(currentPosition.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(currentPosition.toLong()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentPosition.toLong()))
            )
            binding.tvTimeStart.text = "${elapsedTime}"
            handler.postDelayed({ updateTimeAndSeekBar() }, 1000)
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
        binding.waveformSeekBar.isEnabled = false
        checkCut = true
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
        animator.duration = 1000
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
        clearCache()
        mediaPlayer?.release()
        Log.d("fergrhgreher", "destroy ")
        job?.cancel()
        hideLoadingOverlay()
        handler.removeCallbacksAndMessages(null)
    }

    fun cutAndMergeAudio(inputFilePath: String, outputFilePath: String, startTime: String, endTime: String) {
        if (timeCut.equals("00:00")) {
            val copyCommand = "-i \"$inputFilePath\" -c copy \"$outputFilePath\""
            val copyResult = FFmpeg.execute(copyCommand)
            if (copyResult == 0) {
                Log.d("check_audio_mmmm", "Sao chép âm thanh thành công: $outputFilePath")
                val videoUri = Uri.parse(outputFilePath)
                val infoFile = FileInfo.getFileInfoFromPath(videoUri.toString())
                Const.audioCutter = VideoCutterModel(videoUri,timeCut ,infoFile!!.fileSize, infoFile.fileName.toString())
                startActivity(Intent(this@ActivityAudioCutter, SavedActivity::class.java))
            } else {
                Log.d("check_audio_mmmm", "Sao chép âm thanh thất bại. Mã lỗi: $copyResult")
            }
            return
        }
        val firstPartPath = "${cacheDir.path}/first_part.mp3"
        val firstPartCommand = "-i \"$inputFilePath\" -to $startTime -c copy $firstPartPath"
        val firstPartResult = FFmpeg.execute(firstPartCommand)
        // Cắt đoạn từ endTime đến cuối file
        val secondPartPath = "${cacheDir.path}/second_part.mp3"
        val secondPartCommand = "-i \"$inputFilePath\" -ss $endTime -c copy $secondPartPath"
        val secondPartResult = FFmpeg.execute(secondPartCommand)

        if (firstPartResult == 0 && secondPartResult == 0) {
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
//                    checkCut = false
                }else{
//                    checkCut = false
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
                if(countPlay == 0){
                    checkCut = true
                }else{
                    checkCut = false
                }
//                checkCut = false
            }else{
                if(countPlay == 0){
                    checkCut = true
                }else{
                    checkCut = false
                }
//                checkCut = false
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
        selectTypeAudio = "AudioCutter"
        if(binding.loadingOverlay.visibility == View.VISIBLE){
            hideLoadingOverlay()
            isPlaying = false
        }
    }

//    override fun onStop() {
//        super.onStop()
//        if (mediaPlayer!=null && mediaPlayer!!.isPlaying){
//            mediaPlayer!!.pause()
//        }
//        binding.imvPause.visibility = View.GONE
//        binding.imvPlay.visibility = View.VISIBLE
//        isPlaying = false
//    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer!=null && mediaPlayer!!.isPlaying){
            mediaPlayer!!.pause()
        }
        binding.imvPause.visibility = View.GONE
        binding.imvPlay.visibility = View.VISIBLE
        isPlaying = false
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

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if(binding.loadingOverlay.visibility == View.VISIBLE){
            job?.cancel()
            hideLoadingOverlay()
            startCoroutine()
        }else{
            finish()
        }
    }

    fun startCoroutine() {
        FFmpeg.cancel()
        isPlaying = false
        binding.imvPause.visibility = View.GONE
        binding.imvPlay.visibility = View.VISIBLE
    }

}