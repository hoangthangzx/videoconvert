package com.kan.dev.st_042_video_to_mp3.ui
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
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import com.arthenica.mobileffmpeg.FFmpeg
//import com.arthenica.mobileffmpeg.FFmpeg
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivityAudioSpeedBinding
import com.kan.dev.st_042_video_to_mp3.model.AudioInfo
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.Const.audioInfo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionAudioPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionVideoPlay
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.masoudss.lib.SeekBarOnProgressChanged
import com.masoudss.lib.WaveformSeekBar
import com.metaldetector.golddetector.finder.AbsBaseActivity
import java.io.File

class AudioSpeedActivity : AbsBaseActivity<ActivityAudioSpeedBinding>(false) {
    override fun getFragmentID(): Int = 0

    override fun getLayoutId(): Int = R.layout.activity_audio_speed
    var audioUri : Uri? = null
    var itemImvs : List<ImageView> = listOf()
    var mediaPlayer: MediaPlayer? = null
    var valueSpeed = 1f
    var duration = 0

//    private var exoPlayer: ExoPlayer? = null
    override fun init() {
        initData()
        initAction()
        initView()
    }

    private fun initView() {
        binding.waveformSeekBar.setSampleFrom(audioUri!!)
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

    }

//    fun updateWaveformHighlight() {
//        val start = binding.seekBarStart.progress
//        val end = binding.seekBarEnd.progress
//
//        // Giả sử rằng `waveformSeekBar` có max = 100
//        val max = binding.waveformSeekBar.maxProgress
//
//        // Tính tỷ lệ phần sáng từ start -> end
//        val startRatio = start / max.toFloat()
//        val endRatio = end / max.toFloat()
//
//    }

    private fun initAction() {
        binding.imvBack.onSingleClick {
            finish()
        }

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

        binding.imvPlay.onSingleClick {
            binding.imvPause.visibility = View.VISIBLE
            binding.imvPlay.visibility = View.GONE
            mediaPlayer!!.start()
        }

        binding.imvPause.onSingleClick {
            binding.imvPause.visibility = View.GONE
            binding.imvPlay.visibility = View.VISIBLE
            mediaPlayer!!.pause()
        }

        binding.tvDone.onSingleClick {
            val audioPath = getRealPathFromURI(this,audioUri!!)
            val timestamp = System.currentTimeMillis()
            val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
            Log.d("check_audio_link", "initData: "+ musicDir)
            val outputPath = "${musicDir.absolutePath}/${File(audioPath).name.substringBeforeLast(".") }_${timestamp}_speed.mp3"
            if (audioPath != null) {
                Log.d("check_mp3", "initData: "+ audioPath)
                changeAudioSpeed(audioPath, outputPath, valueSpeed)
            }
        }
    }

    private fun initData() {
        itemImvs = listOf(binding.imv1,binding.imv2,binding.imv3,binding.imv4,binding.imv5,binding.imv6,binding.imv7,binding.imv8,binding.imv9,
            binding.imv10,binding.imv11,binding.imv12,binding.imv13,binding.imv14,binding.imv15,binding.imv16,binding.imv17,binding.imv18,
            binding.imv19,binding.imv20,binding.imv21,binding.imv22,binding.imv23,binding.imv24,binding.imv25,binding.imv26,binding.imv27,
            binding.imv28,binding.imv29,binding.imv30,binding.imv31,binding.imv32,binding.imv33,binding.imv34,binding.imv35,binding.imv36)
        audioUri = Const.listAudio[Const.positionAudioPlay].uri
        Log.d("check_audio", "initData: "+ audioUri)
        mediaPlayer = MediaPlayer.create(this,audioUri)
    }

    private fun updateSpeed(progress: Int) {
        valueSpeed = ((progress.toFloat()/10f).toFloat()+ 0.5).toFloat()
        Log.d("check_size", "onProgressChanged: "+(progress/10) + " NNNNN  " + valueSpeed)
        val playbackParams = PlaybackParams()
        playbackParams.speed = valueSpeed
        mediaPlayer!!.playbackParams = playbackParams
        duration = convertTimeToSeconds(listAudio[positionVideoPlay].duration)
        binding.tvNewDuration.text = convertSecondsToTime((duration/valueSpeed).toDouble())
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
    fun changeAudioSpeed(audioUri: String, outputPath: String, speed: Float) {
        // Tạo câu lệnh FFmpeg để thay đổi tốc độ âm thanh
        val command = "-i $audioUri -filter:a \"atempo=$speed\" $outputPath"
        val resultCode = FFmpeg.execute(command)
        if (resultCode == 0) {
            audioInfo = AudioInfo(Uri.parse(outputPath), convertSecondsToTime((duration/valueSpeed).toDouble()), listAudio[positionAudioPlay].sizeInMB,
                listAudio[positionAudioPlay].name, listAudio[positionAudioPlay].date, false, listAudio[positionAudioPlay].mimeType,0)
            Log.d("check_audio_speed", "Thay đổi tốc độ âm thanh thành công")
            startActivity(Intent(this@AudioSpeedActivity,SavedActivity::class.java))
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

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer!!.release()
    }
}