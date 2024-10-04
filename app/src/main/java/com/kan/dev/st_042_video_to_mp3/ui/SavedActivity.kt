package com.kan.dev.st_042_video_to_mp3.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.View
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivitySaveTheConvertedVideoFileBinding
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.Const.audioInfo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.mp3Uri
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionVideoPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectType
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectTypeAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.videoInfo
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.AbsBaseActivity
import java.io.IOException
import java.util.concurrent.TimeUnit

class SavedActivity: AbsBaseActivity<ActivitySaveTheConvertedVideoFileBinding>(false) {
    override fun getFragmentID(): Int = 0

    override fun getLayoutId(): Int =  R.layout.activity_save_the_converted_video_file
    private var isPlaying: Boolean = false
    private lateinit var updateSeekBarRunnable: Runnable
    private val handler = android.os.Handler()
    private var mediaPlayer: MediaPlayer? = null
    override fun init() {
        Log.d("check_uri", "init: "+ Const.mp3Uri)
        mediaPlayer = MediaPlayer()
        initAction()
        if(listVideoPick.size == 1 && selectType.equals("Video")){
            initData()
            initViewFile()
        }
        if(selectTypeAudio.equals("AudioSpeed")){
            initAudioSpeed()
            initDataSpeed()
            initActionSpeed()
        }else if(selectType.equals("Speed")){
            initVideoSpeed()
        }
    }

    private fun initVideoSpeed() {
        binding.ctlVideoSpeed.visibility = View.VISIBLE
        if(Const.videoInfo != null){
            binding.tvTitleVideoSpeed.text = videoInfo!!.name
        }
    }

    private fun initActionSpeed() {
        binding.imvPlay.onSingleClick {
            mediaPlayer!!.start()
        }
    }

    private fun initDataSpeed() {
        mediaPlayer = MediaPlayer.create(this, audioInfo!!.uri)
    }

    private fun initAudioSpeed() {

    }

    private fun initData() {
//        createMediaPlayer()
//        Log.d("check_time__!", "initData: "+ mediaPlayer!!.duration)
////        updateSeekBarRunnable = Runnable {
////            if (mediaPlayer != null && isPlaying) {
////                val totalTime_1 = convertTimeToSeconds(listVideo[positionVideoPlay].duration)
////                val currentTime = (mediaPlayer!!.currentPosition)/1000
////                binding.seekBar.progress = currentTime
////                binding.tvTimeStart.text = formatTime(currentTime)
////                Log.d("check_time", "initData: "+ totalTime_1 + "_____" + currentTime)
////                if (currentTime >= totalTime_1) {
////                    binding.seekBar.progress = binding.seekBar.max // Full thanh seekbar
////                } else {
////                    handler.postDelayed(updateSeekBarRunnable, 10) // Cập nhật mỗi giây
////                }
////            }
////        }
//        startPlaying()
//        initView()
    }

    private fun initView() {
//        binding.seekBar.progress = 0
//        val totalTime = mediaPlayer!!.duration
//        val totalTimeRounded = if (totalTime % 1000 > 0) {
//            totalTime / 1000 * 1000
//        } else {
//            totalTime
//        }
//        binding.seekBar.max = totalTimeRounded
//    }
//
//    private fun createMediaPlayer() {
//        mediaPlayer = MediaPlayer().apply {
//            try {
//                setDataSource(mp3Uri!!.path) // Không cần ?. vì đã kiểm tra tồn tại
//                prepare()
//                Log.d("AudioPlay", "Playback started")
//            } catch (e: IOException) {
//                e.printStackTrace()
//                Log.e("AudioPlay", "Failed to prepare media player: ${e.message}")
//            }
//        }
    }

    @SuppressLint("SetTextI18n")
    private fun initViewFile() {
        binding.tvTitle.isSelected = true
        binding.ctlFile.visibility = View.VISIBLE
        binding.tvTitle.text = listVideo[positionVideoPlay].name
        binding.tvSize.text = listVideo[positionVideoPlay].sizeInMB.toString() + " MB"
        binding.tvDurationVideo.text = listVideo[positionVideoPlay].duration
        binding.tvDuration.text =" / ${listVideo[positionVideoPlay].duration}"
    }

    private fun initAction() {
        binding.imvHome.onSingleClick {
            startActivity(Intent(this@SavedActivity,MainActivity::class.java))
        }

        binding.imvBack.onSingleClick {
            finish()
        }

        binding.imvPlay.onSingleClick {
            binding.imvPlay.visibility = View.GONE
            binding.imvPause.visibility = View.VISIBLE
            startPlaying()
        }

        binding.imvPause.onSingleClick {
            binding.imvPlay.visibility = View.VISIBLE
            binding.imvPause.visibility = View.GONE
            pausePlaying()
        }
    }

    private fun pausePlaying() {
        if (isPlaying) {
            mediaPlayer?.pause()
            isPlaying = false
            Log.d("AudioPlay", "Playback paused")
        }
    }

    private fun formatTime(timeInMillis: Int): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis.toLong()) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun startPlaying() {
        if (mediaPlayer != null) {
            isPlaying = true
            mediaPlayer?.start()
            handler.post(updateSeekBarRunnable)
            mediaPlayer?.setOnCompletionListener {
                binding.imvPlay.visibility = View.VISIBLE
                binding.imvPause.visibility = View.GONE
                isPlaying = false
//                binding.tvTimeStart.text = totalTimeString
                mediaPlayer?.seekTo(0)
                handler.postDelayed({
                    binding.seekBar.progress = 0
                    binding.tvTimeStart.text = "00:00"
                }, 1000)
            }
        }
    }

    fun convertTimeToSeconds(time: String): Int {
        val parts = time.split(":")
        val minutes = parts[0].toInt()
        val seconds = parts[1].toInt()
        return minutes * 60 + seconds // Chuyển đổi thành giây
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.release()
        mediaPlayer = null

    }

}