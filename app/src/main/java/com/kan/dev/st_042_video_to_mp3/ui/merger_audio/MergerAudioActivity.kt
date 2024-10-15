package com.kan.dev.st_042_video_to_mp3.ui.merger_audio
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
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
import com.kan.dev.st_042_video_to_mp3.model.VideoInfo
import com.kan.dev.st_042_video_to_mp3.ui.saved.SavedActivity
import com.kan.dev.st_042_video_to_mp3.utils.Const.audioInfo
import com.kan.dev.st_042_video_to_mp3.utils.Const.audioInformation
import com.kan.dev.st_042_video_to_mp3.utils.Const.countAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSize
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioMerger
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectType
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectTypeAudio
import com.kan.dev.st_042_video_to_mp3.utils.FileInfo
import com.kan.dev.st_042_video_to_mp3.utils.applyGradient
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.AbsBaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.Collections
import java.util.concurrent.TimeUnit
class MergerAudioActivity : AbsBaseActivity<ActivityAudioMergerBinding>(false){
    override fun getFragmentID(): Int  = 0
    override fun getLayoutId(): Int = R.layout.activity_audio_merger
    lateinit var  adapter: MergerAudioAdapter
    lateinit var mediaPlayer: MediaPlayer
    var outputPath =""
    private val handler = android.os.Handler()
    var checkMerger = true
    var MergerUri = ""
    var listVideoPickMerger : MutableList<AudioInfo> = mutableListOf()
    private var isPlaying: Boolean = false
    var audioFilePaths : List<String> = listOf()
    override fun init() {
    }

    private fun initData() {
        mediaPlayer = MediaPlayer()
        if(listAudioMerger.size >= 1){
            listAudioPick = listAudioMerger
            Log.d("check_list_data", "initData: "+ listAudioMerger)
        }
        listVideoPickMerger = listAudioPick
    }

    private fun initView() {
        val colors = intArrayOf(
            ContextCompat.getColor(this@MergerAudioActivity, R.color.color_1),
            ContextCompat.getColor(this@MergerAudioActivity, R.color.color_2)
        )
        binding.tvDone.applyGradient(this@MergerAudioActivity,colors)

        binding.tvTitle.text = "${listVideoPickMerger.size} audio files"
    }

    suspend fun mergeAudioFilesTemp(
        context: Context,
        outputPath: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            if(selectType.equals("Video")){
                audioFilePaths = listVideoPickMerger.mapNotNull { it.uri.toString() }
            }else{
                audioFilePaths = listVideoPickMerger.mapNotNull { getPathFromUri(it.uri, context) }
            }
            Log.d("check_merger", "mergeAudioFilesTemp: $audioFilePaths") // Ghi lại đường dẫn file
            if (audioFilePaths.isEmpty()) {
                Log.e("FFmpeg", "Không có file âm thanh nào để gộp.")
                return@withContext false
            }
//            val convertedFiles = mutableListOf<String>()
//            for (filePath in audioFilePaths) {
////                val validFilePath = filePath?.replace(" ", "\\ ")?.replace("(", "\\(")?.replace(")", "\\)") ?: ""
//                val timestamp = System.currentTimeMillis()
//                Log.d("check_merger", "Chuyển đổi thành công: $filePath")
//                val convertedFilePath = "${filePath.substringBeforeLast(".")}_${timestamp}_converter.mp3"
//                val command = "-i \"$filePath\" -codec:a libmp3lame \"$convertedFilePath\""
//                val rc = FFmpeg.execute(command)
//                if (rc == 0) {
//                    Log.d("check_merger", "Chuyển đổi thành công: $convertedFilePath")
//                    convertedFiles.add(convertedFilePath)
//                } else {
//                    Log.e("FFmpeg", "Lỗi khi chuyển đổi file: $rc")
//                    return@withContext false
//                }
//            }
            val convertedFiles = mutableListOf<String>()
            for (filePath in audioFilePaths) {
                val validFilePath = filePath?.replace(" ", "\\ ")?.replace("(", "\\(")?.replace(")", "\\)") ?: ""
                val timestamp = System.currentTimeMillis()
                Log.d("check_merger", "Chuyển đổi file: $validFilePath")
                val convertedFilePath = "${validFilePath.substringBeforeLast(".")}_${timestamp}_converter.mp3"
                val command = "-i \"$validFilePath\" -codec:a libmp3lame \"$convertedFilePath\""
                val rc = FFmpeg.execute(command)
                if (rc == 0) {
                    Log.d("check_merger", "Chuyển đổi thành công: $convertedFilePath")
                    convertedFiles.add(convertedFilePath)
                } else {
                    return@withContext false
                }
            }
            val fileListPath = "${context.cacheDir}/file_list.txt"
            File(fileListPath).printWriter().use { out ->
                convertedFiles.forEach { out.println("file '$it'") }
            }
            val commandMerge = "-f concat -safe 0 -i \"$fileListPath\" -c copy \"$outputPath\""
            val rcMerge = FFmpeg.execute(commandMerge)
            if (rcMerge == 0) {
                Log.d("check_merger", "Gộp âm thanh thành công.")
                selectTypeAudio = "AudioMerger"
                selectType = ""
                true // Trả về true khi gộp thành công
            } else {
                Log.e("FFmpeg", "Lỗi khi gộp âm thanh: $rcMerge")
                false // Trả về false khi gộp thất bại
            }
        }
    }

    private fun initAction() {
        binding.recFileConvert.itemAnimator = null
        adapter.onClickListener(object : MergerAudioAdapter.onClickItemListener{
            override fun onItemClick(position: Int) {
                if(listVideoPickMerger.size == 1){
                    Toast.makeText(this@MergerAudioActivity, getString(R.string.items_cannot_be_deleted_you_need_at_least_2_items_to_convert), Toast.LENGTH_SHORT).show()
                }else {
                    val pos = listVideoPickMerger[position].pos
                    Log.d("check_sizze_audio_pick", "onItemClick: " + pos + "   "+ listAudioPick)
                    listAudio[pos].active = false
                    countAudio -= 1
                    countSize -= listAudio[pos].sizeInMB.toInt()
                    listVideoPickMerger.removeAt(position)
                    listVideoPick.removeAt(position)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onClickPlay(position: Int, holder: MergerAudioAdapter.ViewHolder) {
                Log.d("cjcjcjcc", "onClickPlay: ")
                val previouslySelectedIndex = listVideoPickMerger.indexOfFirst { it.activePl }
                if (previouslySelectedIndex != -1 && previouslySelectedIndex != position) {
                    listVideoPickMerger[previouslySelectedIndex].activePl = false
                    adapter.notifyItemChanged(previouslySelectedIndex)
                    mediaPlayer?.release()
                }
                if (!listVideoPickMerger[position].activePl) {
                    if(position % 5 == 0){
                        holder.binding.imvVideoFile.setImageResource(R.drawable.imv_pause_1)
                    }else if (position % 5 == 1){
                        holder.binding.imvVideoFile.setImageResource(R.drawable.imv_pause_2)
                    }else if (position % 5 == 2){
                        holder.binding.imvVideoFile.setImageResource(R.drawable.imv_pause_3)
                    }else if (position % 5 == 3){
                        holder.binding.imvVideoFile.setImageResource(R.drawable.imv_pause_4)
                    }else if (position % 5 == 4){
                        holder.binding.imvVideoFile.setImageResource(R.drawable.imv_pause_5)
                    }
                    listVideoPickMerger[position].activePl = true
                    mediaPlayer = setupMediaPlayer(this@MergerAudioActivity, listAudioPick[position].uri)!!
                    mediaPlayer?.start()
                } else {
                    // Nếu item hiện tại đang phát, tạm dừng
                    if(position % 5 == 0){
                        holder.binding.imvVideoFile.setImageResource(R.drawable.icon_play_1)
                    }else if (position % 5 == 1){
                        holder.binding.imvVideoFile.setImageResource(R.drawable.icon_play_3)
                    }else if (position % 5 == 2){
                        holder.binding.imvVideoFile.setImageResource(R.drawable.icon_play_2)
                    }else if (position % 5 == 3){
                        holder.binding.imvVideoFile.setImageResource(R.drawable.icon_play_4)
                    }else if (position % 5 == 4){
                        holder.binding.imvVideoFile.setImageResource(R.drawable.icon_play_5)
                    }
                    listVideoPickMerger[position].activePl = false
                    mediaPlayer?.pause()
                }
                adapter.notifyItemChanged(position)
            }
        })

        binding.imvPause.onSingleClick {
            binding.imvPause.visibility = View.GONE
            binding.imvPlay.visibility = View.VISIBLE
            pausePlaying()
        }

        binding.imvPlay.onSingleClick {
            if(checkMerger == true) {
                binding.progress.visibility = View.VISIBLE
                MergerUri = "${cacheDir.path}/merger.mp3"
                Log.d("check_click", "initAction: thanhhhh anjanah")
                    CoroutineScope(Dispatchers.Main).launch{
                        Log.d("check_audio_link", "initData3: "+ listAudioPick)
                        val isMergedSuccessfully = mergeAudioFilesTemp(this@MergerAudioActivity, MergerUri)
                        if (isMergedSuccessfully) {
                            Log.d("check_click", "cutAudio: "+ MergerUri)
                            binding.progress.visibility = View.GONE
                            createMediaPlayer()
                            binding.seekBarAudio.max = mediaPlayer!!.duration
                            startPlaying()
                            updateTimeAndSeekBar()
                            binding.imvPause.visibility = View.VISIBLE
                            binding.imvPlay.visibility = View.GONE
                            val mergerPath = File(MergerUri)
                            mergerPath.delete()
                            checkMerger = false
                            playVideo()
                        }
                    }
                }else{
                binding.imvPause.visibility = View.VISIBLE
                binding.imvPlay.visibility = View.GONE
                updateTimeAndSeekBar()
                startPlaying()
            }
        }

        binding.imv15Left.setOnClickListener {
            rewindAudio(15000) // Tua về 15 giây
        }
        // Thiết lập sự kiện cho nút tua tới 15 giây
        binding.imv15Right.setOnClickListener {
            forwardAudio(15000) // Tua tới 15 giây
        }

        binding.seekBarAudio.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("DefaultLocale")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer!!.seekTo(progress)
                    val waveformProgress = (progress * 100 / mediaPlayer!!.duration).toFloat()
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

        binding.imvBack.onSingleClick {
            finish()
        }

        binding.tvDone.onSingleClick {
            handler.removeCallbacksAndMessages(null)
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            Log.d("check_audio_link", "initData1: "+ listAudioPick)
            if(listAudioPick.size ==1 ){
                Toast.makeText(this, getString(R.string.you_must_choose_2_or_more_items), Toast.LENGTH_SHORT).show()
            }else{
                showLoadingOverlay()
                val timestamp = System.currentTimeMillis()
                val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
                outputPath = "${musicDir.absolutePath}/${timestamp}_merger.mp3"
                Log.d("check_audio_link", "initData2: "+ listAudioPick)
                CoroutineScope(Dispatchers.Main).launch{
                    Log.d("check_audio_link", "initData3: "+ listAudioPick)
                    val isMergedSuccessfully = mergeAudioFilesTemp(this@MergerAudioActivity, outputPath)
                    if (isMergedSuccessfully) {
                        var audioInfoConverter = FileInfo.getFileInfoFromPath(Uri.parse(outputPath).toString())
                        audioInfo = AudioSpeedModel(Uri.parse(outputPath),audioInfoConverter!!.duration.toString(),audioInfoConverter.fileSize,audioInfoConverter.fileName.toString())
                        startActivity(Intent(this@MergerAudioActivity,SavedActivity::class.java))
                    }
                }
            }
        }
    }

    private fun playVideo() {
        runOnUiThread(object : Runnable {
            override fun run() {
                if (mediaPlayer.isPlaying!!) {
                    val currentPosition = mediaPlayer!!.currentPosition
                    binding.seekBarAudio.progress = currentPosition
                }
                handler.postDelayed(this, 100) // Cập nhật SeekBar mỗi giây
            }
        })
        binding.tvDuration.text = "/ ${formatTimeToHoursMinutes(mediaPlayer!!.duration)}"

        mediaPlayer!!.setOnCompletionListener {
            binding.tvTimeStart.text = formatTimeToHoursMinutes(mediaPlayer.duration)
            handler.postDelayed({
                binding.seekBarAudio.progress = 0
                mediaPlayer!!.seekTo(0)
                binding.tvTimeStart.text = "00:00"
                isPlaying = false
                binding.imvPause.visibility = View.GONE
                binding.imvPlay.visibility = View.VISIBLE
            },1000)
        }
    }

    fun setupMediaPlayer(context: Context, uri: Uri): MediaPlayer? {
        val mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setDataSource(context, uri)
            mediaPlayer.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        mediaPlayer.start()
        return mediaPlayer
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

    private fun formatTimeToHoursMinutes(duration: Int): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
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
                setDataSource(MergerUri) // Không cần ?. vì đã kiểm tra tồn tại
                prepare()
                Log.d("AudioPlay", "Playback started")
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("AudioPlay", "Failed to prepare media player: ${e.message}")
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
                            checkMerger = true
                            Collections.swap(listAudioPick, i, i + 1)
                        }
                    } else {
                        for (i in from downTo to + 1) {
                            checkMerger = true
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

    @SuppressLint("ObjectAnimatorBinding")
    private fun showLoadingOverlay() {
        binding.loadingOverlay.visibility = View.VISIBLE
        val animator = ObjectAnimator.ofInt(binding.lottieAnimationView, "progress", 0, 100)
        animator.duration = 2000 // Thời gian chạy animation (2 giây)
        animator.start()
    }

    private fun hideLoadingOverlay() {
        binding.loadingOverlay.visibility = View.GONE
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

    override fun onResume() {
        super.onResume()
        initData()
        initView()
        initRec()
        initAction()
    }

    override fun onStop() {
        super.onStop()
        hideLoadingOverlay()
        if (mediaPlayer!= null){
            mediaPlayer.release()
        }
        handler.removeCallbacksAndMessages(null)
    }

}