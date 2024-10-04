package com.kan.dev.st_042_video_to_mp3.ui

import android.R.attr.path
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.ExoPlayer
import com.arthenica.mobileffmpeg.FFmpeg
//import com.arthenica.mobileffmpeg.FFmpeg
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivityVideoSpeedBinding
import com.kan.dev.st_042_video_to_mp3.model.VideoInfo
import com.kan.dev.st_042_video_to_mp3.utils.Const.VideoUriSpeed
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionVideoPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.videoInfo
import com.kan.dev.st_042_video_to_mp3.utils.applyGradient
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.AbsBaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class VideoSpeedActivity : AbsBaseActivity<ActivityVideoSpeedBinding>(false) {
    override fun getFragmentID(): Int  = 0
    override fun getLayoutId(): Int = R.layout.activity_video_speed
    var itemImvs : List<ImageView> = listOf()
    var videoUri : Uri? = null
    var valueSpeed = 1f
    var newDuration = ""
    private var exoPlayer: ExoPlayer? = null
    override fun init() {
        initData()
        initView()
        initAction()
    }
    @SuppressLint("SetTextI18n")
    private fun initView() {
        val colors = intArrayOf(
            ContextCompat.getColor(this@VideoSpeedActivity, R.color.color_1),
            ContextCompat.getColor(this@VideoSpeedActivity, R.color.color_2)
        )
        binding.tvDone.applyGradient(this@VideoSpeedActivity,colors)
        binding.textView6.text = "Duration : ${listVideo[positionVideoPlay].duration}"
        binding.tvNewDuration.text = "Duration : ${listVideo[positionVideoPlay].duration}"
    }

    private fun initData() {
        if(listVideo.size>0){
            videoUri = Uri.parse(listVideo[positionVideoPlay].uri.toString())
        }
        exoPlayer = ExoPlayer.Builder(this).build()
        binding.exoVideo.player = exoPlayer
        val mediaItem = MediaItem.fromUri(videoUri!!)
        exoPlayer!!.setMediaItem(mediaItem)
        exoPlayer!!.prepare()
        exoPlayer!!.playWhenReady = true
        itemImvs = listOf(binding.imv1,binding.imv2,binding.imv3,binding.imv4,binding.imv5,binding.imv6,binding.imv7,binding.imv8,binding.imv9,
            binding.imv10,binding.imv11,binding.imv12,binding.imv13,binding.imv14,binding.imv15,binding.imv16,binding.imv17,binding.imv18,
            binding.imv19,binding.imv20,binding.imv21,binding.imv22,binding.imv23,binding.imv24,binding.imv25,binding.imv26,binding.imv27,
            binding.imv28,binding.imv29,binding.imv30,binding.imv31,binding.imv32,binding.imv33,binding.imv34,binding.imv35,binding.imv36)
    }

    private fun initAction() {
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateView(progress)
                updateSpeed(progress)
                Log.d("check_size", "onProgressChanged: "+ progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        binding.imvSwithOff.onSingleClick {
            binding.imvSwithOn.visibility = View.VISIBLE
            binding.imvSwithOff.visibility = View.GONE
            exoPlayer!!.volume = 0f
        }

        binding.imvSwithOn.onSingleClick {
            binding.imvSwithOff.visibility = View.VISIBLE
            binding.imvSwithOn.visibility = View.GONE
            exoPlayer!!.volume = 1f
        }

        binding.imvBack.onSingleClick {
            finish()
        }

        binding.tvDone.onSingleClick {
            binding.ctlProgress.visibility = View.VISIBLE
            val videoPath = getRealPathFromURI(this,videoUri!!)
            val timestamp = System.currentTimeMillis()
            val musicDir = File(Environment.getExternalStorageDirectory(), "Movies/video")
            val outputPath = "${musicDir.absolutePath}/${File(videoPath).name.substringBeforeLast(".") }_${timestamp}_speed.mp4"
            if (videoPath != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    changeVideoSpeed(videoPath, outputPath, valueSpeed)
                    withContext(Dispatchers.Main) {
                        VideoUriSpeed = Uri.parse(outputPath)
                        val file = File(outputPath)
                        val fileName = file.name
                        videoInfo = VideoInfo(
                            VideoUriSpeed!!,
                            newDuration,
                            listVideo[positionVideoPlay].sizeInMB,
                            fileName.toString(),
                            false,
                            0
                        )
                        startActivity(Intent(this@VideoSpeedActivity,SavedActivity::class.java))
                        binding.ctlProgress.visibility = View.GONE // Ẩn progress sau khi hoàn tất
                    }
                }
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

    fun changeVideoSpeed(videoPath: String, outputPath: String, speed: Float): Boolean {
        return try {
            val command =
                "-i $videoPath -filter_complex \"[0:v]setpts=${1 / speed}*PTS[v];[0:a]atempo=$speed[a]\" -map \"[v]\" -map \"[a]\" $outputPath"
            val resultCode = FFmpeg.execute(command)
            if (resultCode == 0) {
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false

        }
    }
//    fun changeVideoSpeed(videoUri: String, outputPath: String, speed: Float) {
//
//        val command = "-i $videoUri -filter_complex \"[0:v]setpts=${1/speed}*PTS[v];[0:a]atempo=$speed[a]\" -map \"[v]\" -map \"[a]\" $outputPath"
//        val resultCode = FFmpeg.execute(command)
//        if (resultCode == 0) {
//            Log.d("check_video_speed", "Thay đổi tốc độ video tc")
//        } else {
//            Log.d("check_video_speed", "Thay đổi tốc độ video thất bại. Mã lỗi: $resultCode")
//        }
//    }

    private fun updateSpeed(progress: Int) {
        valueSpeed = ((progress.toFloat()/10f).toFloat()+ 0.5).toFloat()
        Log.d("check_size", "onProgressChanged: "+(progress/10) + " NNNNN  " + valueSpeed)
        val playbackParameters = PlaybackParameters(valueSpeed.toFloat())
        exoPlayer!!.setPlaybackParameters(playbackParameters)
        val duration = convertTimeToSeconds(listVideo[positionVideoPlay].duration)
        newDuration = convertSecondsToTime((duration/valueSpeed).toDouble())
        binding.tvNewDuration.text = newDuration
        Log.d("check_time", "updateSpeed: "+ convertSecondsToTime((duration/valueSpeed).toDouble()))
    }

    @SuppressLint("SetTextI18n")
    private fun updateView(progress: Int) {
        itemImvs.forEachIndexed { index, imageView ->
            if (index == progress) {
                binding.tvSpeed.text = "${(progress/10.0)+0.5} x"
                imageView.setImageResource(R.drawable.icon_speed_pick)
                val density = resources.displayMetrics.density
                val heightInDp = 15 // Chiều cao mong muốn (15dp)
                val params = imageView.layoutParams
                params.height = (heightInDp * density).toInt() // Chuyển đổi từ dp sang px
                imageView.layoutParams = params
            } else {
                if(index == 5 || index == 15 || index == 25 || index == 35){
                    imageView.setImageResource(R.drawable.line_speed_white)
                }else{
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

    fun convertTimeToSeconds(time: String): Int {
        val parts = time.split(":")
        val minutes = parts[0].toInt()
        val seconds = parts[1].toInt()
        return minutes * 60 + seconds // Chuyển đổi thành giây
    }

    fun convertSecondsToTime(seconds: Double): String {
        val totalSeconds = seconds.toInt() // Chuyển đổi thành giây nguyên
        val minutes = totalSeconds / 60 // Tính phút
        val remainingSeconds = totalSeconds % 60 // Tính giây còn lại
        return String.format("%02d:%02d", minutes, remainingSeconds) // Định dạng về dạng 00:00
    }

    override fun onStop() {
        super.onStop()
        exoPlayer?.release()
    }
}