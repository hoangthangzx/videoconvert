package com.kan.dev.st_042_video_to_mp3.ui.saved

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivitySaveTheConvertedVideoFileBinding
import com.kan.dev.st_042_video_to_mp3.databinding.CustomDialogRingtoneBinding
import com.kan.dev.st_042_video_to_mp3.model.AudioInfo
import com.kan.dev.st_042_video_to_mp3.model.AudioSpeedModel
import com.kan.dev.st_042_video_to_mp3.ui.MainActivity
import com.kan.dev.st_042_video_to_mp3.ui.file_convert_to_mp3.FileConvertAdapter
import com.kan.dev.st_042_video_to_mp3.ui.file_convert_to_mp3.FileConvertToMp3Activity
import com.kan.dev.st_042_video_to_mp3.ui.merger_audio.MergerAudioActivity
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.Const.audioCutter
import com.kan.dev.st_042_video_to_mp3.utils.Const.audioInfo
import com.kan.dev.st_042_video_to_mp3.utils.Const.checkType
import com.kan.dev.st_042_video_to_mp3.utils.Const.countAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSize
import com.kan.dev.st_042_video_to_mp3.utils.Const.currentRingtone
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioMerger
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioSaved
import com.kan.dev.st_042_video_to_mp3.utils.Const.listConvertMp3
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.mp3Uri
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionVideoPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectType
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectTypeAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.videoConvert
import com.kan.dev.st_042_video_to_mp3.utils.Const.videoCutter
import com.kan.dev.st_042_video_to_mp3.utils.Const.videoInfo
import com.kan.dev.st_042_video_to_mp3.utils.SystemUtils
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.AbsBaseActivity
import java.io.IOException
import java.util.concurrent.TimeUnit

class SavedActivity: AbsBaseActivity<ActivitySaveTheConvertedVideoFileBinding>(false) {
    override fun getFragmentID(): Int = 0
    override fun getLayoutId(): Int =  R.layout.activity_save_the_converted_video_file
    private var isPlaying: Boolean = true
    private lateinit var updateSeekBarRunnable: Runnable
    var uriAll : Uri? = null
    lateinit var adapterSaved: AdapterSaved
    lateinit var adapterConverter : FileConvertAdapter
    lateinit var adapterAudioSaved: AdapterAudioSaved
    private val handler = android.os.Handler()
    private var mediaPlayer: MediaPlayer? = null
    override fun init() {
        Log.d("check_uri", "init: "+ Const.mp3Uri)
        mediaPlayer = MediaPlayer()
        initAction()
        binding.tvTitleVideoSpeed.isSelected = true
        if(listVideoPick.size == 1 && selectType.equals("Video")){
            initActionFile()
            initData()
            initViewFile()
        }else if( listVideoPick.size > 1 && selectType.equals("Video")){
            initRec()
            initActionMutil()
            initViewMulti()
        }else if(selectType.equals("VideoCutter")){
            initViewCutter()
            initActionCutter()
        }else if(selectType.equals("VideoCutterToMp3")){
            initViewFile()
            initData()
            initActionCuttertoMp3()
        }
        if(selectTypeAudio.equals("AudioSpeed")){
            initAudioSpeed()
            initData()
            initActionSpeed()
        }else if (selectTypeAudio.equals("AudioConvert")){
            initRecConvert()
            initActionAudioConvert()
        }else if(selectType.equals("VideoConvert")){
            initViewConveter()
            initActionCoberter()
        }else if(selectTypeAudio.equals("AudioMerger")){
            initViewAudiMerger()
            initData()
            initActionFile()
        }
        if(selectTypeAudio.equals("AudioCutter")){
            initActionAudioCutter()
            initData()
            initViewAudioCutter()
        }
    }

    private fun initActionAudioCutter() {
        binding.lnShare.onSingleClick {
            uriAll?.let { shareMp3File(this@SavedActivity, it) }
        }

        binding.lnRingtone.onSingleClick {
            showDialogRingtone()
        }
    }

    private fun initViewAudioCutter() {
        binding.imvPlay.visibility = View.GONE
        binding.imvPause.visibility = View.VISIBLE
        binding.tvTitle.isSelected = true
        binding.ctlFile.visibility = View.VISIBLE
        binding.tvTitle.text = audioCutter!!.name
        binding.tvSize.text = audioCutter!!.sizeInMB.toString()
        binding.tvDurationVideo.text = audioCutter!!.duration
        binding.tvDuration.text =" / ${audioCutter!!.duration}"
    }

    private fun initViewAudiMerger() {
        binding.imvPlay.visibility = View.GONE
        binding.imvPause.visibility = View.VISIBLE
        binding.tvTitle.isSelected = true
        binding.ctlFile.visibility = View.VISIBLE
        binding.tvTitle.text = audioInfo!!.name
        binding.tvSize.text = audioInfo!!.sizeInMB.toString()
        binding.tvDurationVideo.text = audioInfo!!.duration
        binding.tvDuration.text =" / ${audioInfo!!.duration}"
    }

    private fun initActionCoberter() {
        binding.lnShare.onSingleClick {
            shareAudioUrisCv(this@SavedActivity, listAudioSaved)
        }
    }

    private fun initViewConveter() {
        binding.recVideo.visibility = View.VISIBLE
        adapterSaved = AdapterSaved(this)
        adapterSaved.getData(listAudioSaved)
        binding.recVideo.adapter = adapterSaved
        if(listAudioSaved.size == 1){
            binding.lnMerger.visibility = View.GONE
            binding.lnRingtone.visibility = View.VISIBLE
            binding.lnConvert.visibility = View.VISIBLE
        }else{
            binding.lnMerger.visibility = View.GONE
            binding.lnRingtone.visibility = View.GONE
            binding.lnConvert.visibility = View.GONE
        }

    }

    private fun initActionAudioConvert() {
        binding.lnShare.onSingleClick {
            shareAudioUrisCv(this@SavedActivity, listAudioSaved)
        }

        binding.lnMerger.onSingleClick {
            startActivity(Intent(this@SavedActivity,MergerAudioActivity::class.java))
        }

        binding.lnMerger.visibility = View.VISIBLE
        binding.lnRingtone.visibility = View.GONE
    }

    private fun initRecConvert() {
        Log.d("check_audio__", "initRecConvert: okeeeee"+ listAudioSaved)
        binding.recVideo.visibility = View.VISIBLE
        adapterAudioSaved = AdapterAudioSaved(this@SavedActivity)
        adapterAudioSaved.getData(listAudioSaved)
        binding.recVideo.adapter = adapterAudioSaved
    }

    private fun initActionCuttertoMp3() {
        binding.lnShare.onSingleClick {
            shareMp3File(this@SavedActivity, videoConvert!!.uri)
        }

        binding.lnRingtone.onSingleClick {
            showDialogRingtone()
        }

    }

    private fun initViewCuttertoMp3() {
        binding.imvPlay.visibility = View.GONE
        binding.imvPause.visibility = View.VISIBLE
        binding.tvTitle.isSelected = true
        binding.ctlFile.visibility = View.VISIBLE
        binding.tvTitle.text = videoCutter!!.name
        binding.tvSize.text = videoCutter!!.sizeInMB.toString() + " MB"
        binding.tvDurationVideo.text = videoCutter!!.duration
        binding.tvDuration.text =" / ${videoCutter!!.duration}"
    }

    private fun initActionCutter() {
        binding.lnConvert.onSingleClick {
            startActivity(Intent(this@SavedActivity,FileConvertToMp3Activity::class.java))
        }

        binding.lnShare.onSingleClick {
            shareMp3File(this@SavedActivity, videoCutter!!.uri)
        }
    }

    private fun initViewCutter() {
        binding.lnRingtone.visibility = View.GONE
        binding.lnConvert.visibility = View.VISIBLE
        binding.ctlVideoSpeed.visibility = View.VISIBLE
        binding.tvTitleVideoSpeed.text = videoCutter!!.name
        binding.tvDurationVideoSP.text = videoCutter!!.duration
        binding.tvSizeSp.text = videoCutter!!.sizeInMB
        Log.d("check_cutter_uri", "initViewCutter: "+ videoCutter!!.uri)

        val bitmap = getVideoFrameFromUri(this, videoCutter!!.uri)
        bitmap?.let {
            binding.imvVideoImage.setImageBitmap(it)
        }
        binding.tvTitleVideoSpeed.isSelected = true
    }

    private fun getVideoFrameFromUri(context: Context, videoUri: Uri): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, videoUri)
            retriever.getFrameAtTime(0)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            retriever.release()
        }
    }

    private fun initViewMulti() {
        binding.lnRingtone.visibility = View.GONE
        binding.lnMerger.visibility = View.VISIBLE
    }

    private fun initActionMutil() {
        binding.lnShare.onSingleClick {
            shareAudioUris(this@SavedActivity, listConvertMp3)
        }

        binding.lnMerger.onSingleClick {
            startActivity(Intent(this@SavedActivity,MergerAudioActivity::class.java))
        }
    }

    private fun initActionFile() {
        binding.lnShare.onSingleClick {
            uriAll?.let { shareMp3File(this@SavedActivity, it) }
        }

        binding.lnRingtone.onSingleClick {
            showDialogRingtone()
        }
    }

    fun shareAudioUris(context: Context, listAudio: MutableList<String>) {
        // Chuyển đổi danh sách String thành danh sách Uri
        val uriList: ArrayList<Uri> = ArrayList()
        for (audio in listAudio) {
            uriList.add(Uri.parse(audio)) // Chuyển đổi chuỗi thành Uri
        }

        // Tạo Intent để chia sẻ danh sách Uri
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE // Chia sẻ nhiều tệp
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList) // Gửi danh sách Uri
            type = "audio/*" // Định dạng loại tệp
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Cấp quyền đọc cho các ứng dụng nhận
        }

        // Bắt đầu Intent chia sẻ
        context.startActivity(Intent.createChooser(shareIntent, "Share Audio Files"))
    }

    fun shareAudioUrisCv(context: Context, listAudio: MutableList<AudioSpeedModel>) {
        // Chuyển đổi danh sách String thành danh sách Uri
        val uriList: ArrayList<Uri> = ArrayList()
        for (audio in listAudio) {
            uriList.add(Uri.parse(audio.uri.toString())) // Chuyển đổi chuỗi thành Uri
        }

        // Tạo Intent để chia sẻ danh sách Uri
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE // Chia sẻ nhiều tệp
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList) // Gửi danh sách Uri
            type = "audio/*" // Định dạng loại tệp
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Cấp quyền đọc cho các ứng dụng nhận
        }

        // Bắt đầu Intent chia sẻ
        context.startActivity(Intent.createChooser(shareIntent, "Share Audio Files"))
    }

    private fun showDialogRingtone() {
        SystemUtils.setLocale(this)
        val dialogBinding  = CustomDialogRingtoneBinding.inflate(LayoutInflater.from(this))
        val dialog = Dialog(this@SavedActivity)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
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

    private fun setRingtone(type: Int) {
        val customRingtoneUri = Uri.parse(uriAll.toString())
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
        Toast.makeText(this@SavedActivity, R.string.set_ringtone_successfully, Toast.LENGTH_SHORT).show()
    }



    private fun initRec() {
        binding.recVideo.visibility = View.VISIBLE
        adapterSaved = AdapterSaved(this)
        adapterSaved.getData(listAudioSaved)
        binding.recVideo.adapter = adapterSaved
    }

    private fun initVideoSpeed() {
        binding.ctlVideoSpeed.visibility = View.VISIBLE
        if(Const.videoInfo != null){
            binding.tvTitleVideoSpeed.text = videoInfo!!.name
        }
    }

    private fun initActionSpeed() {
        binding.lnShare.onSingleClick {
            uriAll?.let { shareMp3File(this@SavedActivity, it) }
        }

        binding.lnRingtone.onSingleClick {
            showDialogRingtone()
        }
    }

    private fun initAudioSpeed() {
        uriAll = audioInfo!!.uri
        binding.imvPlay.visibility = View.GONE
        binding.imvPause.visibility = View.VISIBLE
        binding.tvTitle.isSelected = true
        binding.ctlFile.visibility = View.VISIBLE
        binding.tvTitle.text = audioInfo!!.name
        binding.tvSize.text = audioInfo!!.sizeInMB.toString()
        binding.tvDurationVideo.text = audioInfo!!.duration
        binding.tvDuration.text =" / ${audioInfo!!.duration}"
    }

    fun shareMp3File(context: Context, mp3Uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/mpeg"  // MIME type cho tệp MP3
            putExtra(Intent.EXTRA_STREAM, mp3Uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)  // Cấp quyền đọc URI cho ứng dụng chia sẻ
        }

        context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ tệp MP3"))
    }

    private fun initData() {
        if(listVideoPick.size == 1 && selectType.equals("Video")){
            uriAll = videoConvert!!.uri
        } else if (selectTypeAudio.equals("AudioSpeed")){
            uriAll = audioInfo!!.uri
        }else if(selectTypeAudio.equals("AudioMerger")){
            uriAll = audioInfo!!.uri
        }else if(selectTypeAudio.equals("AudioCutter")){
            uriAll = audioCutter!!.uri
        }
        else{
            uriAll = videoCutter!!.uri
        }
        createMediaPlayer()
        binding.seekBar.max = mediaPlayer!!.duration
        runOnUiThread(object : Runnable {
            override fun run() {
                if (mediaPlayer != null) {
                    val currentPosition = mediaPlayer!!.currentPosition
                    binding.seekBar.progress = currentPosition
                }
                handler.postDelayed(this, 100) // Cập nhật SeekBar mỗi giây
            }
        })

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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

        mediaPlayer!!.setOnCompletionListener {
            if(selectType.equals("Video")){
                binding.tvTimeStart.text = listVideo[positionVideoPlay].duration
            }else{
                if(videoCutter!= null){
                    binding.tvTimeStart.text = Const.videoCutter!!.duration
                }
            }
            handler.postDelayed({
                binding.seekBar.progress = 0
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

    private fun createMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(uriAll!!.path) // Không cần ?. vì đã kiểm tra tồn tại
                prepare()
                Log.d("AudioPlay", "Playback started")
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("AudioPlay", "Failed to prepare media player: ${e.message}")
            }
        }
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    fun updateTimeAndSeekBar() {
        val mediaPlayer = mediaPlayer ?: return
        val currentPosition = mediaPlayer.currentPosition
        binding.seekBar.progress = currentPosition

        // Hiển thị thời gian đã phát theo format mm:ss
        val elapsedTime = String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(currentPosition.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(currentPosition.toLong()) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentPosition.toLong()))
        )
        binding.tvTimeStart.text = "${elapsedTime}"
        handler.postDelayed({ updateTimeAndSeekBar() }, 1000)
    }

    @SuppressLint("SetTextI18n")
    private fun initViewFile() {
        binding.imvPlay.visibility = View.GONE
        binding.imvPause.visibility = View.VISIBLE
        binding.tvTitle.isSelected = true
        binding.ctlFile.visibility = View.VISIBLE
        binding.tvTitle.text = videoConvert!!.name
        binding.tvSize.text = videoConvert!!.sizeInMB.toString()
        binding.tvDurationVideo.text = videoConvert!!.duration
        binding.tvDuration.text =" / ${videoConvert!!.duration}"
    }

    private fun initAction() {
        binding.imvHome.onSingleClick {
            startActivity(Intent(this@SavedActivity, MainActivity::class.java))
            countAudio = 0
            countSize = 0
            checkType = true
            selectType = ""
            selectTypeAudio = ""
            listVideo.clear()
            listVideoPick.clear()
            listAudio.clear()
            listAudioPick.clear()
            listAudioSaved.clear()
            listConvertMp3.clear()
            audioInfo  = null
        }

        binding.imvBack.onSingleClick {
            listConvertMp3.clear()
            listAudioSaved.clear()
            listAudioMerger.clear()
            finish()
        }

        binding.imvPlay.onSingleClick {
            binding.imvPlay.visibility = View.GONE
            binding.imvPause.visibility = View.VISIBLE
            startPlaying()
            updateTimeAndSeekBar()
        }

        binding.imvPause.onSingleClick {
            binding.imvPlay.visibility = View.VISIBLE
            binding.imvPause.visibility = View.GONE
            pausePlaying()
//            handler.removeCallbacksAndMessages(null)
        }

        binding.imv15Left.setOnClickListener {
            rewindAudio(15000) // Tua về 15 giây
            Log.d("check_clickkkkkkk", "initAction: ")
        }
        // Thiết lập sự kiện cho nút tua tới 15 giây
        binding.imv15Right.setOnClickListener {
            forwardAudio(15000)
            Log.d("check_clickkkkkkk", "initAction: ")// Tua tới 15 giây
        }

    }

    private fun rewindAudio(milliseconds: Int) {
        mediaPlayer?.let {
            val newPosition = it.currentPosition - milliseconds
            if (newPosition >= 0) {
                it.seekTo(newPosition)
            } else {
                it.seekTo(0) // Nếu tua về trước 0, đặt về 0
            }
        }
    }

    // Hàm tua tới audio
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

    private fun formatTime(timeInMillis: Int): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis.toLong()) % 60
        return String.format("%02d:%02d", minutes, seconds)
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
        handler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer!=null){
            mediaPlayer!!.release()
        }
        handler.removeCallbacksAndMessages(null) // Dừng Handler
    }
//    override fun onResume() {
//        super.onResume()
////        if(!mediaPlayer!!.isPlaying && Const.selectType.equals("Video")){
////            initData()
////        }
//    }

}