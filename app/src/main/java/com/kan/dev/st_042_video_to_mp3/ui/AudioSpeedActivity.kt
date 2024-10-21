package com.kan.dev.st_042_video_to_mp3.ui

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.lifecycleScope
import com.arthenica.mobileffmpeg.FFmpeg
//import com.arthenica.mobileffmpeg.FFmpeg
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivityAudioSpeedBinding
import com.kan.dev.st_042_video_to_mp3.model.AudioInfo
import com.kan.dev.st_042_video_to_mp3.model.AudioSpeedModel
import com.kan.dev.st_042_video_to_mp3.ui.saved.SavedActivity
import com.kan.dev.st_042_video_to_mp3.utils.AudioUtils
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.Const.audioInfo
import com.kan.dev.st_042_video_to_mp3.utils.Const.checkDone
import com.kan.dev.st_042_video_to_mp3.utils.Const.countPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.mp3Uri
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionAudioPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionVideoPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectType
import com.kan.dev.st_042_video_to_mp3.utils.FileInfo
import com.kan.dev.st_042_video_to_mp3.utils.applyGradient
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.masoudss.lib.SeekBarOnProgressChanged
import com.masoudss.lib.WaveformSeekBar
import com.metaldetector.golddetector.finder.AbsBaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

@Suppress("UNUSED_EXPRESSION")
class AudioSpeedActivity : AbsBaseActivity<ActivityAudioSpeedBinding>(false) {
    override fun getFragmentID(): Int = 0
    override fun getLayoutId(): Int = R.layout.activity_audio_speed
    var audioUri: Uri? = null
    var audioString: String? = null
    var isUserSeeking = false
    var itemImvs: List<ImageView> = listOf()
    var mediaPlayer: MediaPlayer? = null
    var valueSpeed = 1f
    var duration = 0
    var outputPath =""
    var checkDone = false
    var checkDonePlay = false
    var audioPath = ""
    private var job: Job? = null
    var checkSpeed = true
    var speedUri = ""
    private var isPlaying: Boolean = false
    private val handler = android.os.Handler()
    private val handlerPro = Handler(Looper.getMainLooper())
    private val updateProgress = object : Runnable {
        override fun run() {
            if (mediaPlayer != null) {
                try {
                    if (mediaPlayer!!.isPlaying && !isUserSeeking) {
                        binding.waveformSeekBar.progress =
                            (mediaPlayer!!.currentPosition * 100 / mediaPlayer!!.duration).toFloat()
                        binding.seekBarAudio.progress = mediaPlayer!!.currentPosition
                        handler.postDelayed(this, 100)
                    }
                } catch (e: IllegalStateException) {
                    Log.e("MediaPlayerError", "MediaPlayer is in an illegal state", e)
                }
            }
        }
    }

    override fun init() {
        binding.seekBarAudio.isEnabled = false
        binding.waveformSeekBar.isEnabled = false
        mediaPlayer = MediaPlayer()
        initData()
        initAction()
        initView()
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    private fun initView() {
        binding.tvTimeDr.text = "${listAudio[positionAudioPlay].duration}"
        binding.tvNewDuration.text = " ${listAudio[positionAudioPlay].duration}"
        val colors = intArrayOf(
            ContextCompat.getColor(this@AudioSpeedActivity, R.color.color_1),
            ContextCompat.getColor(this@AudioSpeedActivity, R.color.color_2)
        )
        binding.tvDone.applyGradient(this@AudioSpeedActivity, colors)
        binding.waveformSeekBar.setSampleFrom(audioUri!!)


        binding.waveformSeekBar.onProgressChanged = object :
            SeekBarOnProgressChanged {
            override fun onProgressChanged(
                waveformSeekBar: WaveformSeekBar,
                progress: Float,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    isUserSeeking = true
                    val seekPosition = (mediaPlayer!!.duration * progress / 100)
                    mediaPlayer!!.seekTo(seekPosition.toInt())
                    binding.seekBarAudio.progress = seekPosition.toInt()
                }
            }
        }

        binding.waveformSeekBar.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                isUserSeeking = false
                handler.post(updateProgress)
            }
            false
        }

    }

    fun startPlaying() {
        if (!isPlaying) {
            mediaPlayer?.start()
            isPlaying = true
        }
    }

    fun pausePlaying() {
        if (isPlaying) {
            mediaPlayer?.pause()
            isPlaying = false
        }
    }

    private fun rewindAudio(milliseconds: Int) {
        mediaPlayer?.let {
            val newPosition = it.currentPosition - milliseconds
            if (newPosition >= 0) {
                Log.d("check_new_pos", "rewindAudio: " + newPosition)
                it.seekTo(newPosition)
                binding.waveformSeekBar.progress =
                    (newPosition * 100 / mediaPlayer!!.duration).toFloat()
            } else {
                it.seekTo(0)
                binding.waveformSeekBar.progress = 0f
            }
        }
    }

    // Hàm tua tới audio
    private fun forwardAudio(milliseconds: Int) {
        mediaPlayer?.let {
            val newPosition = it.currentPosition + milliseconds
            if (newPosition <= it.duration) {
                Log.d("check_new_pos", "forwardAudio: " + newPosition)
                it.seekTo(newPosition)
                binding.waveformSeekBar.progress =
                    (newPosition * 100 / mediaPlayer!!.duration).toFloat()
            } else {
                it.seekTo(it.duration)
                binding.waveformSeekBar.progress =
                    (it.duration * 100 / mediaPlayer!!.duration).toFloat()
            }
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun initAction() {
        binding.imvBack.onSingleClick {
            finish()
        }
        binding.progress.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                job?.cancel()
                FFmpeg.cancel()
                binding.progress.visibility = View.GONE
            }
            true
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateView(progress)
                updateSpeed(progress)
                Log.d("check_value_speed", "initAction: " + valueSpeed)
                binding.seekBarAudio.isEnabled = false
                if (checkSpeed == false) {
                    mediaPlayer!!.pause()
                    mediaPlayer!!.seekTo(0)
                }
                checkSpeed = true
                binding.seekBarAudio.progress = 0
                binding.tvTimeStart.text = "00:00"
                binding.tvDuration.text = "/ 00:00"
                binding.imvPause.visibility = View.GONE
                binding.imvPlay.visibility = View.VISIBLE
                Log.d("check_size", "onProgressChanged: " + progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.imvPlay.onSingleClick {
            checkDonePlay = false
            clearCache()
            countPlay +=1
            Log.d("check_value_speed", "initAction: " + valueSpeed)
            if (checkSpeed) {
                val timestamp = System.currentTimeMillis()
                speedUri = "${cacheDir.path}/speed_${timestamp}.mp3"
                audioPath = audioString.toString()
                binding.progress.visibility = View.VISIBLE
                job = CoroutineScope(Dispatchers.Main).launch {
                    val checkValue = changeAudioSpeed(audioPath, speedUri, valueSpeed)
                    Log.d("check_value_speed", "chchchchchchchc: " + valueSpeed)
                    if (checkValue) {
                        binding.waveformSeekBar.isEnabled = true
                        Log.d("check_audio_speed", "dang play")
                        binding.seekBarAudio.isEnabled = true
                        binding.progress.visibility = View.GONE
                        createMediaPlayerSpeed()
                        startPlaying()
                        Log.d("check_audio_speed", "fvgrnrohegjegmbvkermkbhtrnh")
                        checkSpeed = false
                        binding.progress.visibility = View.GONE
                        binding.seekBarAudio.max = mediaPlayer!!.duration
                        binding.imvPause.visibility = View.VISIBLE
                        binding.imvPlay.visibility = View.GONE
                        updateTimeAndSeekBar()
                        playAudio()
                    }
                }
            } else {
                binding.imvPause.visibility = View.VISIBLE
                binding.imvPlay.visibility = View.GONE
                updateTimeAndSeekBar()
                startPlaying()
            }
        }
        binding.tvCancel.onSingleClick {
            finish()
        }
        binding.imvPause.onSingleClick {
            binding.imvPause.visibility = View.GONE
            binding.imvPlay.visibility = View.VISIBLE
            pausePlaying()
//            handler.removeCallbacksAndMessages(null)
        }
        binding.imv15Left.setOnClickListener {
            rewindAudio(15000)
        }
        binding.imv15Right.setOnClickListener {
            forwardAudio(15000)
        }
        binding.tvDone.onSingleClick {
            checkDonePlay = true
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.pause()
            }
            showLoadingOverlay()
            val audioPath = audioString
            val timestamp = System.currentTimeMillis()
//            val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
//            Log.d("check_audio_link", "initData: " + musicDir)
//           outputPath =
//                "${musicDir.absolutePath}/${File(audioPath).name.substringBeforeLast(".")}_${timestamp}_speed.mp3"
            outputPath = "${cacheDir.path}/speed" + "${timestamp}.mp3"
            if (audioPath != null) {
                Log.d("check_mp3", "initData: " + audioPath)
                CoroutineScope(Dispatchers.Main).launch {
                    val isSpeed = changeAudioSpeed(audioPath, outputPath, valueSpeed)
                    Log.d("check_audio_speed", "Thay đổi tốc độ âm thanh thành công" + isSpeed)
                    if (isSpeed == true) {
                        checkDone = true
                        if(countPlay == 0){
                            checkSpeed = true
                        }else{
                            checkSpeed = false
                        }
                        startActivity(Intent(this@AudioSpeedActivity, SavedActivity::class.java))
                    }
                }
            }
        }
    }

    private fun playAudio() {
        val handler = Handler(Looper.getMainLooper())
        val updateProgress = object : Runnable {
            override fun run() {
                if (mediaPlayer != null) {
                    try {
                        if (mediaPlayer != null) {
                            binding.waveformSeekBar.progress =
                                (mediaPlayer!!.currentPosition * 100 / mediaPlayer!!.duration).toFloat()
                            binding.seekBarAudio.progress =
                                mediaPlayer!!.currentPosition
                        }
                        handler.postDelayed(this, 100)

                    } catch (e: IllegalStateException) {
                        Log.e(
                            "MediaPlayerError",
                            "MediaPlayer is in an illegal state",
                            e
                        )
                    }
                }
            }
        }
        mediaPlayer!!.setOnPreparedListener {
            mediaPlayer!!.start()
            handler.post(updateProgress)
        }

        binding.imvPause.visibility = View.VISIBLE
        binding.imvPlay.visibility = View.GONE
//                                runOnUiThread(object : Runnable {
//                                    override fun run() {
//                                        if(mediaPlayer != null ){
//                                            val currentPosition = mediaPlayer!!.currentPosition
//                                            binding.seekBarAudio.progress = currentPosition
//                                        }
//                                        handler.postDelayed(this, 100) // Cập nhật SeekBar mỗi giây
//                                    }
//                                })
        binding.tvDuration.text = "/ ${binding.tvNewDuration.text}"
        mediaPlayer!!.setOnCompletionListener {
            binding.tvTimeStart.text =
                formatTimeToHoursMinutes(mediaPlayer!!.duration)
            handler.postDelayed({
                binding.seekBarAudio.progress = 0
                binding.waveformSeekBar.progress = 0f
                mediaPlayer!!.seekTo(0)
                binding.tvTimeStart.text = "00:00"
                isPlaying = false
                binding.imvPause.visibility = View.GONE
                binding.imvPlay.visibility = View.VISIBLE
//                                    handler.removeCallbacksAndMessages(null)
            }, 1000)
        }

    }

    private fun formatTimeToHoursMinutes(duration: Int): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    @SuppressLint("SetTextI18n")
    private fun initData() {
        itemImvs = listOf(
            binding.imv1,
            binding.imv2,
            binding.imv3,
            binding.imv4,
            binding.imv5,
            binding.imv6,
            binding.imv7,
            binding.imv8,
            binding.imv9,
            binding.imv10,
            binding.imv11,
            binding.imv12,
            binding.imv13,
            binding.imv14,
            binding.imv15,
            binding.imv16,
            binding.imv17,
            binding.imv18,
            binding.imv19,
            binding.imv20,
            binding.imv21,
            binding.imv22,
            binding.imv23,
            binding.imv24,
            binding.imv25,
            binding.imv26,
            binding.imv27,
            binding.imv28,
            binding.imv29,
            binding.imv30,
            binding.imv31,
            binding.imv32,
            binding.imv33,
            binding.imv34,
            binding.imv35,
            binding.imv36
        )
        audioString = getRealPathFromURI(this@AudioSpeedActivity, listAudio[Const.positionAudioPlay].uri)
        audioUri = listAudio[Const.positionAudioPlay].uri
        Log.d("check_audio", "initData: " + audioUri)

        binding.seekBarAudio.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("DefaultLocale")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    isUserSeeking = true
                    mediaPlayer!!.seekTo(progress)
                    val waveformProgress = (progress * 100 / mediaPlayer!!.duration).toFloat()
                    binding.waveformSeekBar.progress = waveformProgress
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
                isUserSeeking = true
                handler.removeCallbacksAndMessages(null)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = false
                updateTimeAndSeekBar()
            }
        })
    }

    @SuppressLint("DefaultLocale")
    private fun updateTimeAndSeekBar() {
        val mediaPlayer = mediaPlayer ?: return
        val currentPosition = mediaPlayer!!.currentPosition
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

    private fun createMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(audioString) // Không cần ?. vì đã kiểm tra tồn tại
                prepare()
                Log.d("AudioPlay___", "Playback started" + audioString)
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("AudioPlay___", "Failed to prepare media player: ${e.message}")
            }
        }
    }

    private fun createMediaPlayerSpeed() {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(speedUri) // Không cần ?. vì đã kiểm tra tồn tại
                prepare()
                Log.d("AudioPlay___", "Playback started" + audioString)
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("AudioPlay___", "Failed to prepare media player: ${e.message}")
            }
        }
    }

    private fun updateSpeed(progress: Int) {
        valueSpeed = ((progress.toFloat() / 10f).toFloat() + 0.5).toFloat()
        Log.d("check_size", "onProgressChanged: " + (progress / 10) + " NNNNN  " + valueSpeed)
        if (valueSpeed == 1f) {
            binding.tvNewDuration.text = listAudio[positionAudioPlay].duration
            binding.tvDuration.text = "/ ${listAudio[positionAudioPlay].duration}"
        } else {
            Log.d(
                "check_time___",
                "updateSpeed: " + duration + "    " + valueSpeed + "     " + convertTimeStringToInt(
                    binding.tvTimeDr.text.toString()
                )
            )
            binding.tvNewDuration.text =
                convertSecondsToTime((convertTimeStringToInt(binding.tvTimeDr.text.toString()) / valueSpeed).toDouble())
            binding.tvDuration.text =
                "/ ${convertSecondsToTime((convertTimeStringToInt(binding.tvTimeDr.text.toString()) / valueSpeed).toDouble())}"
        }
        Log.d(
            "check_time",
            "updateSpeed: " + convertSecondsToTime((duration / valueSpeed).toDouble())
        )
    }

    @SuppressLint("SetTextI18n")
    private fun updateView(progress: Int) {
        itemImvs.forEachIndexed { index, imageView ->
            if (index == progress) {
                binding.tvSpeed.text = "${(progress / 10.0) + 0.5} x"
                imageView.setImageResource(R.drawable.icon_speed_pick)
                val density = resources.displayMetrics.density
                val heightInDp = 15 // Chiều cao mong muốn (15dp)
                val params = imageView.layoutParams
                params.height = (heightInDp * density).toInt() // Chuyển đổi từ dp sang px
                imageView.layoutParams = params
            } else {
                if (index == 5 || index == 15 || index == 25 || index == 35) {
                    imageView.setImageResource(R.drawable.line_speed_white)
                } else {
                    imageView.setImageResource(R.drawable.line_speed)
                    val density = resources.displayMetrics.density
                    val heightInDp = 10 // Chiều cao mong muốn (15dp)
                    val params = imageView.layoutParams
                    params.height = (heightInDp * density).toInt() // Chuyển đổi từ dp sang px
                    imageView.layoutParams = params
                }
            }
        }
    }

    suspend fun changeAudioSpeed(audioUri: String, outputPath: String, speed: Float): Boolean {
        return withContext(Dispatchers.IO) {
            val command = "-i \"$audioUri\" -filter:a \"atempo=$speed\" \"$outputPath\""
            Log.d("check_command", "changeAudioSpeed: " + command)
            val resultCode = FFmpeg.execute(command)
            if (resultCode == 0) {
                if (checkDonePlay){
                    val timestamp = System.currentTimeMillis()
                    val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
                    val path = File("${musicDir.absolutePath}/${timestamp}_speed.mp3")
                    val cacheFile = File(outputPath)
                    cacheFile.copyTo(path, overwrite = true)
                    var audioInfoSpeed =
                        FileInfo.getFileInfoFromPath(Uri.parse(outputPath).toString())
                    audioInfo = AudioSpeedModel(
                        Uri.fromFile(path),
                        audioInfoSpeed!!.duration.toString(),
                        audioInfoSpeed.fileSize,
                        audioInfoSpeed.fileName
                    )
                    Log.d("check_audio_info", "changeAudioSpeed: " + audioInfo)

                }
                Log.d("check_command", "changeAudioSpeed: ytyhth")
                true

            } else {
                Log.d("check_command", "changeAudioSpeed: tbbbbb")
                false
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

    private fun showLoadingOverlay() {
        binding.loadingOverlay.visibility = View.VISIBLE
        val animator = ObjectAnimator.ofInt(binding.lottieAnimationView, "progress", 0, 100)
        animator.duration = 3000 // Thời gian chạy animation (5 giây)
        animator.start()
    }

    private fun hideLoadingOverlay() {
        binding.loadingOverlay.visibility = View.GONE
    }

    fun convertTimeStringToInt(time: String): Int {
        // Tách chuỗi theo dấu ":"
        val parts = time.split(":")

        // Chuyển đổi giờ và phút sang số nguyên
        val hours = parts[0].toInt()
        val minutes = parts[1].toInt()

        // Tính tổng số phút
        return hours * 60 + minutes
    }

    fun convertSecondsToTime(seconds: Double): String {
        val totalSeconds = seconds.toInt() // Chuyển đổi thành giây nguyên
        val minutes = totalSeconds / 60 // Tính phút
        val remainingSeconds = totalSeconds % 60 // Tính giây còn lại
        return String.format("%02d:%02d", minutes, remainingSeconds) // Định dạng về dạng 00:00
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

    override fun onResume() {
        super.onResume()
//        clearCache()
        job?.cancel()
//        if (binding.loadingOverlay.visibility == View.VISIBLE) {
//            checkDonePlay = true
//            val audioPath = audioString
//            val timestamp = System.currentTimeMillis()
////            val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
////            Log.d("check_audio_link", "initData: " + musicDir)
//            val outputPath =
//                "${cacheDir.path}/speed${timestamp}.mp3"
//            if (audioPath != null) {
//                Log.d("check_mp3", "initData: " + audioPath)
//                CoroutineScope(Dispatchers.Main).launch {
//                    val isSpeed = changeAudioSpeed(audioPath, outputPath, valueSpeed)
//                    Log.d("check_audio_speed", "Thay đổi tốc độ âm thanh thành công" + isSpeed)
//                    if (isSpeed == true) {
//                        checkSpeed = false
//                        var audioInfoSpeed =
//                            FileInfo.getFileInfoFromPath(Uri.parse(outputPath).toString())
//                        audioInfo = AudioSpeedModel(
//                            Uri.parse(outputPath),
//                            audioInfoSpeed!!.duration.toString(),
//                            audioInfoSpeed.fileSize,
//                            audioInfoSpeed.fileName.toString()
//                        )
//                        startActivity(Intent(this@AudioSpeedActivity, SavedActivity::class.java))
//
//                    }
//                }
//            }
//        } else if (binding.progress.visibility == View.VISIBLE) {
//            checkDonePlay = false
//            val timestamp = System.currentTimeMillis()
//            speedUri = "${cacheDir.path}/speed${timestamp}.mp3"
//            audioPath = audioString.toString()
//            binding.progress.visibility = View.VISIBLE
//            job = CoroutineScope(Dispatchers.Main).launch {
//                val checkValue = changeAudioSpeed(audioPath, speedUri, valueSpeed)
//                Log.d("check_value_speed", "chchchchchchchc: " + valueSpeed)
//                if (checkValue) {
//                    Log.d("check_audio_speed", "dang play")
//                    binding.seekBarAudio.isEnabled = true
//                    binding.progress.visibility = View.GONE
//                    createMediaPlayerSpeed()
//                    startPlaying()
//                    Log.d("check_audio_speed", "fvgrnrohegjegmbvkermkbhtrnh")
//                    checkSpeed = false
//                    binding.progress.visibility = View.GONE
//                    binding.seekBarAudio.max = mediaPlayer!!.duration
//                    updateTimeAndSeekBar()
//                    playAudio()
//                }else{
//                    clearCache()
//                }
//            }
//        } else {
            binding.imvPause.visibility = View.GONE
            binding.imvPlay.visibility = View.VISIBLE
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
        clearCache()
        hideLoadingOverlay()
        handler.removeCallbacksAndMessages(null) // Dừng tất cả Runnable
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer!!.release()
        }
    }

    override fun onStop() {
        super.onStop()
//        FFmpeg.cancel()
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
            checkSpeed = false
        }
        binding.imvPause.visibility = View.GONE
        binding.imvPlay.visibility = View.VISIBLE
        isPlaying = false
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (binding.loadingOverlay.visibility == View.VISIBLE) {
            hideLoadingOverlay()
            job?.cancel()
            startCoroutine()
        } else {
            finish()
        }
    }

    fun startCoroutine() {
        job = CoroutineScope(Dispatchers.Main).launch {
            FFmpeg.cancel()
            clearCache()
            isPlaying = false
        }
        binding.imvPause.visibility = View.GONE
        binding.imvPlay.visibility = View.VISIBLE
    }

}