package com.kan.dev.st_042_video_to_mp3.ui

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivityAudioCutterBinding
import com.masoudss.lib.SeekBarOnProgressChanged
import com.masoudss.lib.WaveformSeekBar
import com.metaldetector.golddetector.finder.AbsBaseActivity

class ActivityAudioCutter : AbsBaseActivity<ActivityAudioCutterBinding>(false) {
    override fun getFragmentID(): Int  = 0
    override fun getLayoutId(): Int = R.layout.activity_audio_cutter
    private var isPlaying: Boolean = true
    var mediaPlayer: MediaPlayer? = null
    private val handler = android.os.Handler()
    override fun init() {
        initData()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initData() {
//        binding.waveformSeekBar.setSampleFrom(audioUri!!)
        val handler = Handler(Looper.getMainLooper())
        val updateProgress = object : Runnable {
            override fun run() {
                if (mediaPlayer != null) {
                    try {
                        if (mediaPlayer!!.isPlaying) {
                            binding.waveformSeekBar.progress = (mediaPlayer!!.currentPosition * 100 / mediaPlayer!!.duration).toFloat()
                            handler.postDelayed(this, 100)
                        }
                    } catch (e: IllegalStateException) {
                        Log.e("MediaPlayerError", "MediaPlayer is in an illegal state", e)
                    }
                }
            }
        }
        mediaPlayer!!.setOnPreparedListener {
            mediaPlayer!!.start()
            handler.post(updateProgress)
        }

        binding.waveformSeekBar.onProgressChanged = object :
            SeekBarOnProgressChanged {
            override fun onProgressChanged(
                waveformSeekBar: WaveformSeekBar,
                progress: Float,
                fromUser: Boolean
            ) {
                if(fromUser){
                    val seekPosition = (mediaPlayer!!.duration * progress / 100)
                    mediaPlayer!!.seekTo(seekPosition.toInt())
                }
            }
        }
        binding.waveformSeekBar.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                handler.post(updateProgress)  // Tiếp tục cập nhật sau khi dừng kéo
            }
            false
        }
    }
}