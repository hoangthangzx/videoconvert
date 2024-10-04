package com.kan.dev.st_042_video_to_mp3.ui.audio_converter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.arthenica.mobileffmpeg.FFmpeg
//import com.arthenica.mobileffmpeg.FFmpeg
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivityAudioConverterBinding
import com.kan.dev.st_042_video_to_mp3.ui.SavedActivity
import com.kan.dev.st_042_video_to_mp3.ui.file_convert_to_mp3.FileConvertAdapter
import com.kan.dev.st_042_video_to_mp3.ui.select_audio.SelectAudioAdapter
import com.kan.dev.st_042_video_to_mp3.utils.Const.countAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSize
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoPick
import com.kan.dev.st_042_video_to_mp3.utils.applyGradient
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.AbsBaseActivity
import java.net.URLEncoder

class AudioConverterActivity: AbsBaseActivity<ActivityAudioConverterBinding>(false) {
    override fun getFragmentID(): Int = 0
    override fun getLayoutId(): Int = R.layout.activity_audio_converter
    lateinit var adapter: AudioConverterAdapter
    var imvItems : List<LinearLayout> = listOf()
    var audioType  = ""
    var audioUri : Uri? = null
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
                val pos = listAudioPick[position].pos
                listAudioPick.removeAt(position)
                listAudio[pos].active = false
                countAudio -= 1
                countSize -= listAudio[pos].sizeInMB.toInt()
                adapter.notifyDataSetChanged()
            }
        })

        binding.imvBack.onSingleClick {
            finish()
        }

        imvItems.forEachIndexed { index, imvItem ->
            imvItem.onSingleClick {
                imvItems.forEach {
                    it.setBackgroundResource(R.drawable.bg_item_convert)
                }
                imvItem.setBackgroundResource(R.drawable.bg_item_convert_pick)
                Log.d("check_style", "initAction: ")
                when(index){
                    0 -> audioType ="mp3"
                    1 -> audioType ="flac"
                    2 -> audioType ="acc"
                    3 -> audioType ="ogg"
                    4 -> audioType ="wav"
                    5 -> audioType ="wma"
                    6 -> audioType ="ac3"
                }
            }
        }
        binding.tvDone.onSingleClick {
            val audioPath = getRealPathFromURI(this,audioUri!!)
            val timestamp = System.currentTimeMillis()
            val outputPath = "${filesDir.absolutePath}/output_$timestamp.mp3"
            if (audioPath != null) {
                convertAudio(audioPath,outputPath,audioType)
            }
        }
    }

    fun convertAudio(inputPath: String, outputPath: String, format: String) {
//        val command = "-i $inputPath -vn -ar 44100 -ac 2 -b:a 192k $outputPath.$format"
//        val command = "-i \"$inputPath\" -vn -ar 44100 -ac 2 -b:a 192k \"$outputPath.$format\""
        val fixedInputPath = inputPath.replace(" ", "\\ ")
        val fixedOutputPath = outputPath.replace(" ", "\\ ")
        val command = "-i \"$fixedInputPath\" -vn -ar 44100 -ac 2 -b:a 192k \"$fixedOutputPath.$format\""
//        val fixedInputPath = URLEncoder.encode(inputPath, "UTF-8")
//        val fixedOutputPath = URLEncoder.encode(outputPath, "UTF-8")
//        val command = "-i \"$fixedInputPath\" -vn -ar 44100 -ac 2 -b:a 192k \"$fixedOutputPath.$format\""
        Log.d("check_mp3", "Chuyển đổi : $command")

        val resultCode = FFmpeg.execute(command)
        if (resultCode == 0) {
            Log.d("check_mp3", "Chuyển đổi thành cng ")
            startActivity(Intent(this@AudioConverterActivity, SavedActivity::class.java))
        } else {
            Log.d("check_mp3", "Chuyển đổi thất bại. Mã lỗi: $resultCode")
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

    private fun initData() {
        audioUri = Uri.parse(listAudioPick[0].uri.toString())
        adapter = AudioConverterAdapter(this@AudioConverterActivity)
        adapter.getData(listAudioPick)
        binding.recFileConvert.adapter = adapter
        imvItems = listOf(binding.lnMp3,binding.lnFLAC,binding.lnAcc,binding.lnOgg,binding.lnWAV,binding.lnWMA,binding.lnAc3)
    }
}