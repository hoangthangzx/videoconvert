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
import androidx.lifecycle.lifecycleScope
import com.arthenica.mobileffmpeg.ExecuteCallback
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
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectTypeAudio
import com.kan.dev.st_042_video_to_mp3.utils.FileInfo
import com.kan.dev.st_042_video_to_mp3.utils.applyGradient
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.AbsBaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor

class AudioConverterActivity : AbsBaseActivity<ActivityAudioConverterBinding>(false) {
    override fun getFragmentID(): Int = 0
    override fun getLayoutId(): Int = R.layout.activity_audio_converter
    lateinit var adapter: AudioConverterAdapter
    var imvItems: List<LinearLayout> = listOf()
    var audioType = ""
    var checkItem = false
    var outputPath = ""
    var shouldCancel = false
    var listOutputPath = mutableListOf<File>()
    var audioUri: Uri? = null
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
        binding.tvDone.applyGradient(this@AudioConverterActivity, colors)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initAction() {
        binding.loadingOverlay.setOnTouchListener { _, _ ->
            true
        }
        adapter.onClickListener(object : AudioConverterAdapter.onClickItemListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onItemClick(position: Int) {
                if (listAudioPick.size == 1) {
                    Toast.makeText(
                        this@AudioConverterActivity,
                        getString(R.string.items_cannot_be_deleted_you_need_at_least_1_items_to_convert),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
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
                when (index) {
                    0 -> audioType = "mp3"
                    1 -> audioType = "flac"
                    2 -> audioType = "aac"
                    3 -> audioType = "ogg"
                    4 -> audioType = "wav"
                    5 -> audioType = "wma"
                    6 -> audioType = "ac3"
                }
                Log.d("check_style", "initAction: " + audioType)
            }
        }
        binding.tvDone.onSingleClick {
            if (checkItem == false) {
                Toast.makeText(
                    this@AudioConverterActivity,
                    getString(R.string.you_must_choose_a_format),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (listAudioPick.size == 0) {
                Toast.makeText(
                    this@AudioConverterActivity,
                    getString(R.string.you_must_choose_1_file),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                showLoadingOverlay()
                if (!isConverting) { // Kiểm tra xem có đang chuyển đổi hay không
                    isConverting = true
                    job = CoroutineScope(Dispatchers.Main).launch {
                        convertAllSongsToMp3()
                    }
                }
            }
        }
    }
    fun convertAudio(inputPath: String, outputPath: String, format: String) {
        val command = "-i \"$inputPath\" -vn -ar 44100 -ac 2 -b:a 192k \"$outputPath\""
        Log.d("check_mp3", "Chuyển đổi : $command")
        val resultCode = FFmpeg.execute(command)
//        resultCodex = FFmpeg.executeAsync(command, object : ExecuteCallback {
//            override fun apply(executionId: Long, returnCode: Int) {
//                Log.d("check_return", "apply: " + executionId + "____" + returnCode)
//                listOutputPath.add(File(outputPath))
//                Log.d(
//                    "check_iust_out_put_path",
//                    "convertAudio: " + listOutputPath + listOutputPath.size + "  " + listAudioPick
//                )
//                if (listOutputPath.size == listAudioPick.size) {
//                    listOutputPath.forEachIndexed { index, cacheFile ->
//                        Log.d(
//                            "check_iust_out_put_path",
//                            "convertAudio: " + index + "  ____  " + cacheFile
//                        )
//                        val timestamp = System.currentTimeMillis()
//                        val musicDir =
//                            File(Environment.getExternalStorageDirectory(), "Music/music")
//                        val path = File("${musicDir.absolutePath}/${listAudioPick[index].name.substringBeforeLast(".")}_${timestamp}_convert.${format}")
//                        cacheFile.copyTo(path, overwrite = true)
//                        var audioInfoConverter = FileInfo.getFileInfoFromPath(Uri.parse(path.toString()).toString())
//                        audioInformation = AudioInfo(
//                            Uri.parse(path.toString()),
//                            audioInfoConverter!!.duration.toString(),
//                            convertMbToBytes(
//                                audioInfoConverter.fileSize.toString()
//                            ),
//                            audioInfoConverter.fileName,
//                            getFormattedDate(),
//                            false,
//                            "mp3",
//                            0,
//                            false
//                        )
//                        listAudioMerger.add(audioInformation!!)
//                        audioInfo = AudioSpeedModel(
//                            Uri.parse(path.toString()),
//                            audioInfoConverter!!.duration.toString(),
//                            audioInfoConverter.fileSize,
//                            audioInfoConverter.fileName.toString()
//                        )
//                        listAudioSaved.add(audioInfo!!)
//                    }
//                    lifecycleScope.launch {
//                        // Khởi động Activity mới
//                        startActivity(Intent(this@AudioConverterActivity, SavedActivity::class.java))
//
//                        // Trì hoãn 500ms trước khi gọi hideLoadingOverlay
//                        delay(500)
//
//                        // Gọi hideLoadingOverlay() sau khi đã khởi động Activity mới
//                        hideLoadingOverlay()
//                    }
//                }
//
//            }
//
//        })

        /*resultCode = FFmpeg.executeAsync(command, object :
           ExecuteCallback {
           override fun apply(executionId: Long, returnCode: Int) {

           }
       }, object:Executor{
            override fun execute(command: Runnable?) {
                listOutputPath.add(File(outputPath))
                Log.d("check_iust_out_put_path", "convertAudio: "+ listOutputPath  + listOutputPath.size + "  " + listAudioPick.size)
                if (listOutputPath.size == listAudioPick.size) {
                    listOutputPath.forEachIndexed { index, cacheFile ->
                        Log.d("check_iust_out_put_path", "convertAudio: "+ index  + "  ____  "+ cacheFile)
                        val timestamp = System.currentTimeMillis()
                        val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
                        val path = File("${musicDir.absolutePath}/${timestamp}_convert_$index.mp3")
                        cacheFile.copyTo(path, overwrite = true)
                        var audioInfoConverter = FileInfo.getFileInfoFromPath(cacheFile.toString())
                        audioInformation = AudioInfo(Uri.parse(outputPath),audioInfoConverter!!.duration.toString(),convertMbToBytes(audioInfoConverter.fileSize.toString()
                        ), audioInfoConverter.fileName, getFormattedDate(),false, "mp3", 0, false  )
                        listAudioMerger.add(audioInformation!!)
                        audioInfo = AudioSpeedModel(Uri.parse(outputPath),audioInfoConverter!!.duration.toString(),audioInfoConverter.fileSize,audioInfoConverter.fileName.toString())
                        listAudioSaved.add(audioInfo!!)
                    }
                }
            }

        } );*/
        // FFmpeg.cancel(resultCode)
        if (resultCode == 0) {
            listOutputPath.add(File(outputPath))
            Log.d(
                "check_iust_out_put_path",
                "convertAudio: " + listOutputPath + listOutputPath.size + "  " + listAudioPick.size
            )
//            if (listOutputPath.size == listAudioPick.size) {
//                listOutputPath.forEachIndexed { index, cacheFile ->
//                    Log.d(
//                        "check_iust_out_put_path",
//                        "convertAudio: " + index + "  ____  " + cacheFile
//                    )
//                    val timestamp = System.currentTimeMillis()
//                    val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
//                    val path = File("${musicDir.absolutePath}/${timestamp}_convert_$index.mp3")
//                    cacheFile.copyTo(path, overwrite = true)
//                    var audioInfoConverter = FileInfo.getFileInfoFromPath(cacheFile.toString())
//                    audioInformation = AudioInfo(
//                        Uri.parse(outputPath),
//                        audioInfoConverter!!.duration.toString(),
//                        convertMbToBytes(
//                            audioInfoConverter.fileSize.toString()
//                        ),
//                        audioInfoConverter.fileName,
//                        getFormattedDate(),
//                        false,
//                        "mp3",
//                        0,
//                        false
//                    )
//                    listAudioMerger.add(audioInformation!!)
//                    audioInfo = AudioSpeedModel(
//                        Uri.parse(outputPath),
//                        audioInfoConverter!!.duration.toString(),
//                        audioInfoConverter.fileSize,
//                        audioInfoConverter.fileName.toString()
//                    )
//                    listAudioSaved.add(audioInfo!!)
//                }
//            }
            if (listOutputPath.size == listAudioPick.size) {
                listOutputPath.forEachIndexed { index, cacheFile ->
                    Log.d(
                        "check_iust_out_put_path",
                        "convertAudio: " + index + "  ____  " + cacheFile
                    )
                    val timestamp = System.currentTimeMillis()
                    val musicDir =
                        File(Environment.getExternalStorageDirectory(), "Music/music")
                    val path = File(
                        "${musicDir.absolutePath}/${
                            listAudioPick[index].name.substringBeforeLast(".")
                        }_${timestamp}_convert.${format}"
                    )
                    cacheFile.copyTo(path, overwrite = true)
                    var audioInfoConverter =
                        FileInfo.getFileInfoFromPath(Uri.parse(path.toString()).toString())
                    audioInformation = AudioInfo(
                        Uri.parse(path.toString()),
                        audioInfoConverter!!.duration.toString(),
                        convertMbToBytes(
                            audioInfoConverter.fileSize.toString()
                        ),
                        audioInfoConverter.fileName,
                        getFormattedDate(),
                        false,
                        "mp3",
                        0,
                        false
                    )
                    listAudioMerger.add(audioInformation!!)
                    audioInfo = AudioSpeedModel(
                        Uri.parse(path.toString()),
                        audioInfoConverter!!.duration.toString(),
                        audioInfoConverter.fileSize,
                        audioInfoConverter.fileName.toString()
                    )
                    listAudioSaved.add(audioInfo!!)
                }
                Log.d("check---------------", "convertAudio: thahahahahahaaha " + audioInfo)

            }
        } else {
            Log.d("check_mp3", "Chuyển đổi thất bại. Mã lỗi: $resultCode")
        }
    }

    private suspend fun convertAllSongsToMp3() {
        listAudioSaved.clear()
        listAudioMerger.clear()
        listOutputPath.clear()
        withContext(Dispatchers.IO) { // Chạy trong IO context
            for (audio in listAudioPick) {
                Log.d("check_iust_out_put_path", "convertAllSongsToMp3: _____" + listAudioPick.size)
                val audioPath = getRealPathFromURI(this@AudioConverterActivity, audio.uri!!)
                val timestamp = System.currentTimeMillis()
//                val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
                outputPath = "${cacheDir.path}/converter_cache_${timestamp}.${audioType}"
                if (audioPath != null) {
                    convertAudio(audioPath, outputPath, audioType)
                }
                if (shouldCancel) { // shouldCancel là một biến boolean mà bạn có thể đặt điều kiện
                    break // Hủy vòng lặp
                }
            }
        }
        lifecycleScope.launch {
            startActivity(Intent(this@AudioConverterActivity, SavedActivity::class.java))
            delay(500)
            hideLoadingOverlay()
        }

    //        startActivity(Intent(this@AudioConverterActivity, SavedActivity::class.java))
    }

    fun convertMbToBytes(sizeString: String): Long {
        val numericString = sizeString.replace(" MB", "").replace(",", ".").trim()
        val mbSize = numericString.toDouble()
        return (mbSize * 1024 * 1024).toLong() // Nhân để chuyển đổi từ MB sang bytes
    }

    fun getFormattedDate(): String {
        val currentDate = Date() // Lấy ngày hiện tại
        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault()) // Định dạng ngày
        return dateFormat.format(currentDate) // Trả về chuỗi ngày đã định dạng
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
        shouldCancel = true
        if (binding.loadingOverlay.visibility == View.VISIBLE) {
            job?.cancel()
            hideLoadingOverlay()
            startCoroutine()
        } else {
            finish()
        }
    }
    override fun onStop() {
        super.onStop()
    }
    private fun initData() {
        adapter = AudioConverterAdapter(this@AudioConverterActivity)
        adapter.getData(listAudioPick)
        binding.recFileConvert.adapter = adapter
        imvItems = listOf(
            binding.lnMp3,
            binding.lnFLAC,
            binding.lnAcc,
            binding.lnOgg,
            binding.lnWAV,
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        shouldCancel = true
        job?.cancel()
        clearCache()
    }

    override fun onResume() {
        super.onResume()
        selectTypeAudio = "AudioConvert"
        isConverting = false
    }

    fun startCoroutine() {
        FFmpeg.cancel()
        isConverting = false
        listAudioSaved.clear()
        listAudioMerger.clear()
        listOutputPath.clear()
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
}