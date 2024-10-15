package com.kan.dev.st_042_video_to_mp3.ui

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ContentValues
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
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivityVideoConverterBinding
import com.kan.dev.st_042_video_to_mp3.model.AudioSpeedModel
import com.kan.dev.st_042_video_to_mp3.ui.file_convert_to_mp3.FileConvertAdapter
import com.kan.dev.st_042_video_to_mp3.ui.saved.SavedActivity
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSizeVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.countVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioSaved
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoPick
import com.kan.dev.st_042_video_to_mp3.utils.FileInfo
import com.kan.dev.st_042_video_to_mp3.utils.VideoUtils
import com.kan.dev.st_042_video_to_mp3.utils.applyGradient
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.AbsBaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class VideoConverterActivity : AbsBaseActivity<ActivityVideoConverterBinding>(false) {
    override fun getFragmentID(): Int = 0
    override fun getLayoutId(): Int = R.layout.activity_video_converter
    lateinit var adapter: FileConvertAdapter
    var imvItems : List<LinearLayout> = listOf()
    var audioType  = ""
    var checkItem = false
    private var job: Job? = null // Khai báo Job
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob()) // CoroutineScope chính
    private var isConverting = false
    override fun init() {
        initDataMulti()
        initViewMutil()
        initActionMulti()
    }
    private fun initActionMulti() {
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
                Log.d("check_style", "initAction: ")
                when(index){
                    0 -> audioType ="3gpp"
                    1 -> audioType ="mp4"
                    2 -> audioType ="mov"
                    3 -> audioType ="flv"
                    4 -> audioType ="mkv"
                    5 -> audioType ="avi"
                    6 -> audioType ="mv4"
                    7 -> audioType ="mts"
                    8 -> audioType ="m2ts"
                    9-> audioType ="ts"
                }
            }
        }

        binding.imvBack.onSingleClick {
            finish()
        }
        adapter.onClickListener(object : FileConvertAdapter.onClickItemListener{
            override fun onItemClick(position: Int) {
                if(listVideoPick.size == 1){
                    Toast.makeText(this@VideoConverterActivity, getString(R.string.items_cannot_be_deleted_you_need_at_least_1_items_to_convert), Toast.LENGTH_SHORT).show()
                }else {
                    val pos = listVideoPick[position].pos
                    listVideo[pos].active = false
                    countVideo -= 1
                    countSizeVideo -= listVideo[pos].sizeInMB.toInt()
                    listVideoPick.removeAt(position)
                    adapter.notifyDataSetChanged()
                }
            }
        })
        binding.tvDone.onSingleClick {
            if(checkItem== false || listVideoPick.size == 0){
                Toast.makeText(this@VideoConverterActivity, getString(R.string.there_are_no_items_to_convert), Toast.LENGTH_SHORT).show()
            }else{
                showLoadingOverlay()
                if (!isConverting) {
                    job = CoroutineScope(Dispatchers.Main).launch {
                        isConverting = true
                        convertAllVideoToSong()
                    }
                }
            }
        }
    }
    private suspend fun convertAllVideoToSong() {
        val currentVideoList = listVideoPick.toList()
        withContext(Dispatchers.IO) { // Chạy trong IO context
            for (video in currentVideoList) {
                val videoPath = getRealPathFromURI(this@VideoConverterActivity, video.uri!!)
                val timestamp = System.currentTimeMillis()
                val originalFileName = File(videoPath).name
                val baseFileName = originalFileName.substringBeforeLast(".", originalFileName)
                val videoDir = File(Environment.getExternalStorageDirectory(), "Movies/video")
                val outputPath = "${videoDir.absolutePath}/${baseFileName}_${timestamp}_convert.${audioType}" // Thay audioType nếu cần
                if (videoPath != null) {
                    convertVideo(videoPath, outputPath, audioType)
                }
            }
        }
        startActivity(Intent(this, SavedActivity::class.java))
        isConverting = false
    }

    private fun convertVideo(videoPath: String, outputPath: String, audioType: String) {
        val fixedInputPath = videoPath.replace(" ", "\\ ")
        val fixedOutputPath = outputPath.replace(" ", "\\ ")
        val outputFileName = if (fixedOutputPath.endsWith(".$audioType")) {
            fixedOutputPath
        } else {
            "$fixedOutputPath.$audioType"
        }
        val command = "-i \"$fixedInputPath\" \"$outputFileName"
        Log.d("check_video", "Chuyển đổi video : $command")
        val resultCode = FFmpeg.execute(command)
        if (resultCode == 0) {
            VideoUtils.saveVideoToMediaStore(this,outputFileName,audioType)
            val videoInfoConverter = FileInfo.getFileInfoFromPath(Uri.parse(outputPath).toString())
            val videoInfo = AudioSpeedModel(Uri.parse(outputPath), videoInfoConverter!!.duration.toString(), videoInfoConverter.fileSize, videoInfoConverter.fileName.toString())
            listAudioSaved.add(videoInfo)
        } else {
            Log.d("check_video", "Chuyển đổi thất bại. Mã lỗi: $resultCode")
        }
    }

    private fun showLoadingOverlay() {
        binding.loadingOverlay.visibility = View.VISIBLE
        val animator = ObjectAnimator.ofInt(binding.lottieAnimationView, "progress", 0, 100)
        animator.duration = 1000L // Thời gian chạy animation (5 giây)
        animator.start()
    }

    private fun hideLoadingOverlay() {
        binding.loadingOverlay.visibility = View.GONE
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

    private fun initDataMulti() {
        adapter = FileConvertAdapter(this@VideoConverterActivity)
        adapter.getData(listVideoPick)
        binding.recFileConvert.adapter = adapter
        imvItems = listOf(binding.ln3Gp,binding.lnMp4,binding.lnMov,binding.lnFlv,binding.lnMkv,binding.lnAvi
            ,binding.lnM4v,binding.lnMts,binding.lnM2ts,binding.lnTs)
    }

    private fun initViewMutil() {
        val colors = intArrayOf(
            ContextCompat.getColor(this@VideoConverterActivity, R.color.color_1),
            ContextCompat.getColor(this@VideoConverterActivity, R.color.color_2)
        )
        binding.tvDone.applyGradient(this@VideoConverterActivity,colors)

        binding.recFileConvert.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()

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

    override fun onStop() {
        super.onStop()
        hideLoadingOverlay()
    }
}