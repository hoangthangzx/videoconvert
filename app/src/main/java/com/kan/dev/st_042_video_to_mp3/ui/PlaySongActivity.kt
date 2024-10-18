package com.kan.dev.st_042_video_to_mp3.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivityPlayAudioBinding
import com.kan.dev.st_042_video_to_mp3.databinding.CustomDialogRenameBinding
import com.kan.dev.st_042_video_to_mp3.databinding.CustomDialogRingtoneBinding
import com.kan.dev.st_042_video_to_mp3.databinding.CustomeDialogDeleteBinding
import com.kan.dev.st_042_video_to_mp3.model.AudioInfo
import com.kan.dev.st_042_video_to_mp3.utils.Const.audioInfo
import com.kan.dev.st_042_video_to_mp3.utils.Const.audioInformation
import com.kan.dev.st_042_video_to_mp3.utils.Const.currentRingtone
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioStorage
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionAudioPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectFr
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectType
import com.kan.dev.st_042_video_to_mp3.utils.Const.uriPlay
import com.kan.dev.st_042_video_to_mp3.utils.SystemUtils
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.masoudss.lib.SeekBarOnProgressChanged
import com.masoudss.lib.WaveformSeekBar
import com.metaldetector.golddetector.finder.AbsBaseActivity
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class PlaySongActivity : AbsBaseActivity<ActivityPlayAudioBinding>(false) {
    override fun getFragmentID(): Int  = 0
    override fun getLayoutId(): Int = R.layout.activity_play_audio
    private val handler = android.os.Handler()
    private val handlerPro = Handler(Looper.getMainLooper())
    var mediaPlayer: MediaPlayer? = null
    var isUserSeeking = false
    var audioUri : Uri? = null
    private var isPlaying: Boolean = true
    var audioString : String? = null
    private val updateProgress = object : Runnable {
        override fun run() {
            if (mediaPlayer != null) {
                try {
                    if (mediaPlayer!!.isPlaying && !isUserSeeking) {
                        // Cập nhật progress cho cả hai SeekBar
                        binding.waveformSeekBar.progress = (mediaPlayer!!.currentPosition * 100 / mediaPlayer!!.duration).toFloat()
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
        initData()
        initDataWaveForn()
        initAction()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initAction() {
        binding.imvBack.onSingleClick {
            finish()
        }

        binding.lnSetas.onSingleClick {
            showDialogRingtone()
        }

        binding.lnShare.onSingleClick {
            uriPlay?.let { shareAudioFile(this@PlaySongActivity, it) }
            mediaPlayer!!.pause()
            binding.imvPause.visibility = View.GONE
            binding.imvPlay.visibility = View.VISIBLE
        }
//
//        binding.lnDelete.onSingleClick {
//            showDialogDelete()
//        }
//
//        binding.lnRename.onSingleClick {
//            showDialogRename()
//        }

        binding.imvTick.onSingleClick {
            binding.lnMenu.visibility = View.GONE
            binding.lnMenu.visibility = View.VISIBLE
        }

        binding.bgTouch.setOnTouchListener { v, event ->
            if (binding.lnMenu.visibility == View.VISIBLE) {
                binding.lnMenu.visibility = View.GONE // Ẩn LinearLayout
                true
            } else {
                false
            }
        }

        binding.imvPlay.onSingleClick {
            binding.imvPause.visibility = View.VISIBLE
            binding.imvPlay.visibility = View.GONE
            startPlaying()
            handlerPro.post(updateProgress)
            updateTimeAndSeekBar()
        }

        binding.imvPause.onSingleClick {
            binding.imvPause.visibility = View.GONE
            binding.imvPlay.visibility = View.VISIBLE
            pausePlaying()
//            handler.removeCallbacksAndMessages(null)
        }

        binding.imv15Left.setOnClickListener {
            rewindAudio(15000) // Tua về 15 giây
        }
        // Thiết lập sự kiện cho nút tua tới 15 giây
        binding.imv15Right.setOnClickListener {
            forwardAudio(15000) // Tua tới 15 giây
        }
    }

    private fun showDialogRename() {
        SystemUtils.setLocale(this)
        val dialogBinding  = CustomDialogRenameBinding.inflate(LayoutInflater.from(this))
        val dialog = Dialog(this)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)
        dialogBinding.lnOk.onSingleClick{


            dialog.dismiss()
        }
        dialogBinding.lnCancel.onSingleClick {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showDialogDelete() {
        SystemUtils.setLocale(this)
        val dialogBinding  = CustomeDialogDeleteBinding.inflate(LayoutInflater.from(this))
        val dialog = Dialog(this)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)
        dialogBinding.tvYes.onSingleClick{
            listAudioStorage.removeAt(positionAudioPlay)
            deleteFileFromAudioInfo(this, audioInformation!!)
            dialog.dismiss()
        }
        dialogBinding.tvNo.onSingleClick {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun deleteFileFromAudioInfo(context: Context, audioInfo: AudioInfo): Boolean {
        val fileUri = audioInfo.uri
        return try {
            val contentResolver: ContentResolver = context.contentResolver
            val rowsDeleted = contentResolver.delete(fileUri, null, null) // Xóa file
            rowsDeleted > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun showDialogRingtone() {
        SystemUtils.setLocale(this)
        val dialogBinding  = CustomDialogRingtoneBinding.inflate(LayoutInflater.from(this))
        val dialog = Dialog(this)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.window?.setBackgroundDrawable(getDrawable(R.color.transparent))
        dialog.setContentView(dialogBinding.root)
        var position = 0
        dialog.setCancelable(false)
        dialogBinding.rdGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
            for (i in 0 until radioGroup.childCount) {
                val radioButton = radioGroup.getChildAt(i) as? RadioButton
                if (radioButton?.id == checkedId) {
                    position = i
                    break
                }
            }
        }
        dialogBinding.lnSave.onSingleClick{
            when(position){
                0 -> handleWriteSettingsPermission(0)
                1 -> handleWriteSettingsPermission(1)
                2 -> handleWriteSettingsPermission(2)
            }
            currentRingtone = position
            dialog.dismiss()
        }
        dialogBinding.lnCancel.onSingleClick {
            dialog.dismiss()
        }
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun handleWriteSettingsPermission(type: Int) {
        val permission: Boolean = Settings.System.canWrite(this)
        if (permission) {
            setRingtone(type)
        } else {
            openAndroidPermissionsMenu()
        }
    }

    private fun openAndroidPermissionsMenu() {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.setData(Uri.parse("package:" + this.packageName))
        startActivity(intent)
    }

    fun shareAudioFile(context: Context, uri: Uri) {
        val filePath = uri.toString().replace("file://", "")
        val file = File(filePath)
        val uri = FileProvider.getUriForFile(
            this,
            "${this.packageName}.provider",  // Package của ứng dụng bạn
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        if (shareIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(Intent.createChooser(shareIntent, "Share Audio File"))
        }
    }

    private fun setRingtone(type: Int) {
        val customRingtoneUri = Uri.parse(uriPlay!!.toString())
        when (type) {
            0       -> {
                RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, customRingtoneUri)
            }

            1 -> {
                RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION, customRingtoneUri)
            }

            2-> {
                RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM, customRingtoneUri)
            }

            else                                                   -> {}
        }
        Toast.makeText(this@PlaySongActivity, R.string.set_ringtone_successfully, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initDataWaveForn() {
        uriPlay?.let { binding.waveformSeekBar.setSampleFrom(it.toString()) }
        Log.d("check_uriPlay", "initDataWaveForn: "+ uriPlay)
        val handler = Handler(Looper.getMainLooper())
        val updateProgress = object : Runnable {
            override fun run() {
                if (mediaPlayer != null) {
                    try {
                        mediaPlayer?.let { mp ->
                            if (mp.isPlaying) {
                                binding.waveformSeekBar.progress = (mp.currentPosition * 100 / mp.duration).toFloat()
                                binding.seekBarAudio.progress = mp.currentPosition
                            }
                        }
                        handler.postDelayed(this, 100)
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
                    isUserSeeking = true
                    val seekPosition = (mediaPlayer!!.duration * progress / 100)
                    mediaPlayer!!.seekTo(seekPosition.toInt())
                    binding.seekBarAudio.progress = seekPosition.toInt()
                }
            }
        }

        binding.waveformSeekBar.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                isUserSeeking = false  // Đánh dấu người dùng đã dừng kéo SeekBar
                handler.post(updateProgress)  // Tiếp tục cập nhật sau khi dừng kéo
            }
            false
        }
    }
    private fun initData() {
        binding.tvNameSong.isSelected = true
        binding.tvShare.isSelected = true
        binding.tvSetAs.isSelected = true
        binding.imvPause.visibility = View.VISIBLE
        binding.imvPlay.visibility = View.GONE
        if( selectType.equals("Video")){
            binding.tvNameSong.text =  audioInformation!!.name
            audioString = audioInformation!!.uri.toString()
            audioUri = audioInformation!!.uri
        }else if (selectFr.equals("Fr")){
            binding.tvNameSong.text =  audioInformation!!.name
            audioString = audioInformation!!.uri.toString()
            audioUri = audioInformation!!.uri
        }else{
            binding.tvNameSong.text =  audioInfo!!.name
            audioString = audioInfo!!.uri.toString()
            audioUri = audioInfo!!.uri
        }

        Log.d("check_audio", "initData: "+ audioUri)
        createMediaPlayer()
        binding.seekBarAudio.max = mediaPlayer!!.duration
        if(selectType.equals("Video")){
            binding.tvDuration.text = "/ ${audioInformation!!.duration}"
        }else if (selectFr.equals("Fr")){
            binding.tvDuration.text = "/ ${audioInformation!!.duration}"

        }else{
            binding.tvDuration.text = "/ ${audioInfo!!.duration}"
        }
        runOnUiThread(object : Runnable {
            override fun run() {
                if (mediaPlayer != null ) {
                    val currentPosition = mediaPlayer!!.currentPosition
                    binding.seekBarAudio.progress = currentPosition
                }
                handler.postDelayed(this, 100) // Cập nhật SeekBar mỗi giây
            }
        })

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
        mediaPlayer!!.setOnCompletionListener {
            binding.tvTimeStart.text = audioInformation!!.duration
            handler.postDelayed({
                binding.seekBarAudio.progress = 0
                binding.waveformSeekBar.progress = 0f
                mediaPlayer!!.seekTo(0)
                binding.tvTimeStart.text = "00:00"
                isPlaying = false
                binding.imvPause.visibility = View.GONE
                binding.imvPlay.visibility = View.VISIBLE
                handler.removeCallbacksAndMessages(null)
            },1000)

        }
        mediaPlayer!!.start()
        updateTimeAndSeekBar()
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
                it.seekTo(it.duration) // Nếu tua tới sau khi bài hát kết thúc, đặt về cuối bài hát
            }
        }
    }
    @SuppressLint("DefaultLocale")
    private fun updateTimeAndSeekBar() {
        val mediaPlayer = mediaPlayer ?: return
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

    private fun createMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(uriPlay.toString()) // Không cần ?. vì đã kiểm tra tồn tại
                prepare()
                Log.d("AudioPlay___", "Playback started"+ audioString)
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("AudioPlay___", "Failed to prepare media player: ${e.message}")
            }
        }
    }
    override fun onStop() {
        super.onStop()
        pausePlaying()
        binding.imvPlay.visibility = View.VISIBLE
        binding.imvPause.visibility = View.GONE
    }
    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer!=null){
            mediaPlayer!!.release()
        }
        handler.removeCallbacksAndMessages(null) // Dừng Handler
    }

}