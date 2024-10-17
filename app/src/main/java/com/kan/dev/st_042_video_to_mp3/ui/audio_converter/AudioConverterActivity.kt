package com.kan.dev.st_042_video_to_mp3.ui.audio_converter

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.arthenica.mobileffmpeg.FFmpeg
//import com.arthenica.mobileffmpeg.FFmpeg
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivityAudioConverterBinding
import com.kan.dev.st_042_video_to_mp3.model.AudioInfo
import com.kan.dev.st_042_video_to_mp3.model.AudioSpeedModel
import com.kan.dev.st_042_video_to_mp3.ui.saved.SavedActivity
import com.kan.dev.st_042_video_to_mp3.utils.Const.audioInfo
import com.kan.dev.st_042_video_to_mp3.utils.Const.audioInformation
import com.kan.dev.st_042_video_to_mp3.utils.Const.countAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSize
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioMerger
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioSaved
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoPick
import com.kan.dev.st_042_video_to_mp3.utils.FileInfo
import com.kan.dev.st_042_video_to_mp3.utils.applyGradient
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.AbsBaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AudioConverterActivity: AbsBaseActivity<ActivityAudioConverterBinding>(false) {
    override fun getFragmentID(): Int = 0
    override fun getLayoutId(): Int = R.layout.activity_audio_converter
    lateinit var adapter: AudioConverterAdapter
    var imvItems : List<LinearLayout> = listOf()
    var audioType  = ""
    var checkItem = false
    var audioUri : Uri? = null
    private var job: Job? = null
    private var isConverting = false
    override fun init() {
        initData()
        initView()
        initAction()
    }

    private fun initView() {
        val colors = intArrayOf(
            ContextCompat.getColor(this@AudioConverterActivity, R.color.color_1),
            ContextCompat.getColor(this@AudioConverterActivity, R.color.color_2)
        )
        binding.tvDone.applyGradient(this@AudioConverterActivity,colors)
    }

    private fun initAction() {
        adapter.onClickListener(object : AudioConverterAdapter.onClickItemListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onItemClick(position: Int) {
                if(listAudioPick.size == 1){
                    Toast.makeText(this@AudioConverterActivity, getString(R.string.items_cannot_be_deleted_you_need_at_least_1_items_to_convert), Toast.LENGTH_SHORT).show()
                }else {
                    val pos = listAudioPick[position].pos
                    listAudioPick.removeAt(position)
                    listAudio[pos].active = false
                    countAudio -= 1
                    countSize -= listAudio[pos].sizeInMB.toInt()
                    adapter.notifyDataSetChanged()
                }
            }
        })
        binding.imvBack.onSingleClick {
            finish()
        }

        binding.tvCancel.onSingleClick {
            finish()
        }

        imvItems.forEachIndexed { index, imvItem ->
            imvItem.onSingleClick {
                checkItem = true
                imvItems.forEach {
                    it.setBackgroundResource(R.drawable.bg_item_convert)
                }
                imvItem.setBackgroundResource(R.drawable.bg_item_convert_pick)
                when(index){
                    0 -> audioType ="mp3"
                    1 -> audioType ="flac"
                    2 -> audioType ="aac"
                    3 -> audioType ="ogg"
                    4 -> audioType ="wav"
                    5 -> audioType ="wma"
                    6 -> audioType ="ac3"
                }

                Log.d("check_style", "initAction: "+ audioType)

            }
        }


        binding.tvDone.onSingleClick {
            if(listAudioPick.size == 0){
                Toast.makeText(this@AudioConverterActivity, getString(R.string.you_must_choose_2_or_more_items), Toast.LENGTH_SHORT).show()
            }else{
                showLoadingOverlay()
                    if (!isConverting) { // Kiểm tra xem có đang chuyển đổi hay không
                        isConverting = true
                        // Gọi hàm chuyển đổi với Coroutine
                        job = CoroutineScope(Dispatchers.Main).launch {
                            convertAllSongsToMp3()
                        }
                    }

            }
        }
    }
    fun convertAudio(inputPath: String, outputPath: String, format: String) {
        val command = "-i \"$inputPath\" -vn -ar 44100 -ac 2 -b:a 192k \"$outputPath\""
//        val command = "-i \"$inputPath\" -vn -ar 44100 -ac 2 -b:a 192k -codec:a libmp3lame \"$outputPath\""
        Log.d("check_mp3", "Chuyển đổi : $command")
        val resultCode = FFmpeg.execute(command)
        if (resultCode == 0) {
                var audioInfoConverter = FileInfo.getFileInfoFromPath(Uri.parse("$outputPath").toString())
                audioInfo = AudioSpeedModel(Uri.parse("$outputPath"),audioInfoConverter!!.duration.toString(),audioInfoConverter.fileSize,audioInfoConverter.fileName.toString())
                listAudioSaved.add(audioInfo!!)
                Log.d("check---------------", "convertAudio: "+ audioInfo)
        } else {
            Log.d("check_mp3", "Chuyển đổi thất bại. Mã lỗi: $resultCode")
        }
    }
    private suspend fun convertAllSongsToMp3() {
        withContext(Dispatchers.IO) { // Chạy trong IO context
            for(audio in listAudioPick){
                val audioPath = getRealPathFromURI(this@AudioConverterActivity,audio.uri!!)
                val timestamp = System.currentTimeMillis()
                val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
                val outputPath = "${musicDir.absolutePath}/${File(audioPath).name.substringBeforeLast(".") }_${timestamp}_convert.${audioType}"
                if (audioPath != null) {
                    convertAudio(audioPath,outputPath,audioType)
                }
            }
        }
        startActivity(Intent(this, SavedActivity::class.java))
        isConverting = false
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
        animator.duration = 2000 // Thời gian chạy animation (5 giây)
        animator.start()
    }

    private fun hideLoadingOverlay() {
        binding.loadingOverlay.visibility = View.GONE
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


    private fun initData() {
//        audioUri = Uri.parse(listAudioPick[0].uri.toString())
        adapter = AudioConverterAdapter(this@AudioConverterActivity)
        adapter.getData(listAudioPick)
        binding.recFileConvert.adapter = adapter
        imvItems = listOf(binding.lnMp3,binding.lnFLAC,binding.lnAcc,binding.lnOgg,binding.lnWAV,binding.lnWMA,binding.lnAc3)
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
        hideLoadingOverlay()
    }
}