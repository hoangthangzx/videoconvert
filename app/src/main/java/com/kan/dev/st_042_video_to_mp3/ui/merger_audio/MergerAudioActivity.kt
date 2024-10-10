package com.kan.dev.st_042_video_to_mp3.ui.merger_audio

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.END
import androidx.recyclerview.widget.ItemTouchHelper.START
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView
import com.arthenica.mobileffmpeg.FFmpeg
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivityAudioMergerBinding
import com.kan.dev.st_042_video_to_mp3.model.AudioInfo
import com.kan.dev.st_042_video_to_mp3.model.AudioSpeedModel
import com.kan.dev.st_042_video_to_mp3.ui.saved.SavedActivity
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.Const.audioInfo
import com.kan.dev.st_042_video_to_mp3.utils.Const.countAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSize
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioSaved
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoPick
import com.kan.dev.st_042_video_to_mp3.utils.FileInfo
import com.kan.dev.st_042_video_to_mp3.utils.applyGradient
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.AbsBaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Collections

class MergerAudioActivity : AbsBaseActivity<ActivityAudioMergerBinding>(false){
    override fun getFragmentID(): Int  = 0
    override fun getLayoutId(): Int = R.layout.activity_audio_merger
    lateinit var  adapter: MergerAudioAdapter
    lateinit var mediaPlayer: MediaPlayer
    var outputPath =""
    override fun init() {
        initView()
        initRec()
        initAction()
    }

    private fun initView() {
        val colors = intArrayOf(
            ContextCompat.getColor(this@MergerAudioActivity, R.color.color_1),
            ContextCompat.getColor(this@MergerAudioActivity, R.color.color_2)
        )
        binding.tvDone.applyGradient(this@MergerAudioActivity,colors)
    }

    suspend fun mergeAudioFilesTemp(
        context: Context,
        listAudio: List<AudioInfo>,
        outputPath: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            val audioFilePaths = listAudio.mapNotNull { getPathFromUri(it.uri, context) }
            Log.d("check_merger", "mergeAudioFilesTemp: $audioFilePaths") // Ghi lại đường dẫn file
            if (audioFilePaths.isEmpty()) {
                Log.e("FFmpeg", "Không có file âm thanh nào để gộp.")
                return@withContext false
            }
            val convertedFiles = mutableListOf<String>()
            for (filePath in audioFilePaths) {
                val validFilePath = filePath?.replace(" ", "\\ ")?.replace("(", "\\(")?.replace(")", "\\)") ?: ""
                val timestamp = System.currentTimeMillis()
                Log.d("check_merger", "Chuyển đổi thành công: $validFilePath")
                val convertedFilePath = "${validFilePath.substringBeforeLast(".")}_${timestamp}_converter.mp3"
                val command = "-i \"$validFilePath\" -codec:a libmp3lame $convertedFilePath"
                val rc = FFmpeg.execute(command)
                if (rc == 0) {
                    Log.d("check_merger", "Chuyển đổi thành công: $convertedFilePath")
                    convertedFiles.add(convertedFilePath)
                } else {
                    Log.e("FFmpeg", "Lỗi khi chuyển đổi file: $rc")
                    return@withContext false
                }
            }
            val fileListPath = "${context.cacheDir}/file_list.txt"
            File(fileListPath).printWriter().use { out ->
                convertedFiles.forEach { out.println("file '$it'") }
            }
            val commandMerge = "-f concat -safe 0 -i $fileListPath -c copy $outputPath"
            val rcMerge = FFmpeg.execute(commandMerge)
            if (rcMerge == 0) {
                Log.d("check_merger", "Gộp âm thanh thành công.")
                true // Trả về true khi gộp thành công
            } else {
                Log.e("FFmpeg", "Lỗi khi gộp âm thanh: $rcMerge")
                false // Trả về false khi gộp thất bại
            }
        }
    }

    private fun initAction() {
        adapter.onClickListener(object : MergerAudioAdapter.onClickItemListener{
            override fun onItemClick(position: Int) {
                val pos = listAudioPick[position].pos
                listAudio[pos].active = false
                countAudio -= 1
                countSize -= listAudio[pos].sizeInMB.toInt()
                listAudioPick.removeAt(position)
                adapter.notifyDataSetChanged()
            }
        })

        binding.imvPlay.onSingleClick {

        }

        binding.imvBack.onSingleClick {
            finish()
        }

        binding.tvDone.onSingleClick {
            showLoadingOverlay()
            val timestamp = System.currentTimeMillis()
            val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
            Log.d("check_audio_link", "initData: "+ musicDir)
            outputPath = "${musicDir.absolutePath}/${timestamp}_merger.mp3"
            CoroutineScope(Dispatchers.Main).launch{
                val isMergedSuccessfully = mergeAudioFilesTemp(this@MergerAudioActivity, listAudioPick, outputPath)
                if (isMergedSuccessfully) {
                    var audioInfoConverter = FileInfo.getFileInfoFromPath(Uri.parse(outputPath).toString())
                    audioInfo = AudioSpeedModel(Uri.parse(outputPath),audioInfoConverter!!.duration.toString(),audioInfoConverter.fileSize,audioInfoConverter.fileName.toString())
                    startActivity(Intent(this@MergerAudioActivity,SavedActivity::class.java))
                }
            }
        }
    }

    private fun initRec() {
        adapter = MergerAudioAdapter(this)
        adapter.getData(listAudioPick)
        binding.recFileConvert.adapter = adapter
        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(
                UP or DOWN or START or END,
                0) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder): Boolean {
                    val adapter = recyclerView.adapter
                    val from = viewHolder.adapterPosition
                    val to = target.adapterPosition
                    if (from < to) {
                        for (i in from until to) {
                            Collections.swap(listAudioPick, i, i + 1)
                        }
                    } else {
                        for (i in from downTo to + 1) {
                            Collections.swap(listAudioPick, i, i - 1)
                        }
                    }
                    adapter?.notifyItemMoved(from, to)
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                }

                override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                    super.clearView(recyclerView, viewHolder)
                }
            }
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.recFileConvert)

    }

    private fun showLoadingOverlay() {
        binding.loadingOverlay.visibility = View.VISIBLE
        val animator = ObjectAnimator.ofInt(binding.lottieAnimationView, "progress", 0, 100)
        animator.duration = 3000 // Thời gian chạy animation (5 giây)
        animator.start()
    }

    private fun hideLoadingOverlay() {
        binding.loadingOverlay.visibility = View.GONE
    }

    fun getPathFromUri(uri: Uri, context: Context): String? {
        var path: String? = null
        val projection = arrayOf(MediaStore.Audio.Media.DATA)
        val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                path = it.getString(columnIndex)
            }
        }
        return path
    }

    override fun onDestroy() {
        super.onDestroy()
        hideLoadingOverlay()
    }
}