package com.kan.dev.st_042_video_to_mp3.ui.merger_audio
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.END
import androidx.recyclerview.widget.ItemTouchHelper.START
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView
import com.arthenica.mobileffmpeg.FFmpeg
import com.arthenica.mobileffmpeg.FFprobe
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivityAudioMergerBinding
import com.kan.dev.st_042_video_to_mp3.model.AudioSpeedModel
import com.kan.dev.st_042_video_to_mp3.model.ElementCount
import com.kan.dev.st_042_video_to_mp3.ui.saved.SavedActivity
import com.kan.dev.st_042_video_to_mp3.utils.Const.audioInfo
import com.kan.dev.st_042_video_to_mp3.utils.Const.countAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.countMap
import com.kan.dev.st_042_video_to_mp3.utils.Const.countPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSize
import com.kan.dev.st_042_video_to_mp3.utils.Const.elementCounts
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioMerger
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioPickMerger
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectType
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectTypeAudio
import com.kan.dev.st_042_video_to_mp3.utils.FileInfo
import com.kan.dev.st_042_video_to_mp3.utils.SystemUtils
import com.kan.dev.st_042_video_to_mp3.utils.applyGradient
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.AbsBaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
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
    var mediaPlayer: MediaPlayer? = null
    var mediaPlayer_1: MediaPlayer? = null
    var outputPath =""
    private val handler = android.os.Handler()
    var checkMerger = true
    var MergerUri = ""
    var checkDone = false
    private var job: Job? = null
    private var isPlaying: Boolean = false
    var audioFilePaths : List<String> = listOf()
    @RequiresApi(Build.VERSION_CODES.N)
    override fun init() {
        initData()
        initView()
        initRec()
        initAction()
        binding.tvCancel.onSingleClick {
            finish()
        }
    }
    @RequiresApi(Build.VERSION_CODES.N)
    private fun initData() {
        mediaPlayer = MediaPlayer()
        if(selectType.equals("Video")){
            listAudioPickMerger = listAudioMerger.map { it.copy() }.toMutableList()
        }else{
            listAudioPickMerger = listAudioPick.map { it.copy() }.toMutableList()
        }
        listAudioPickMerger.forEach { it.activePl = false }
        binding.seekBarAudio.isEnabled = false
    }
    private fun initView() {
        val colors = intArrayOf(
            ContextCompat.getColor(this@MergerAudioActivity, R.color.color_1),
            ContextCompat.getColor(this@MergerAudioActivity, R.color.color_2)
        )
        binding.tvDone.applyGradient(this@MergerAudioActivity,colors)
        binding.tvTitle.text = "${listAudioPickMerger.size} audio files"
    }
    suspend fun mergeAudioFilesTemp(
        context: Context,
        outputPath: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            if (!isActive) return@withContext false
            if(selectType.equals("Video")){
                audioFilePaths = listAudioPickMerger.mapNotNull { it.uri.toString() }
            }else{
                audioFilePaths = listAudioPickMerger.mapNotNull { getPathFromUri(it.uri, context) }
            }
            Log.d("check_merger", "mergeAudioFilesTemp: $audioFilePaths") // Ghi lại đường dẫn file
            if (audioFilePaths.isEmpty()) {
                Log.e("FFmpeg", "Không có file âm thanh nào để gộp.")
                return@withContext false
            }
            val convertedFiles = mutableListOf<String>()
            for (filePath in audioFilePaths) {
                convertedFiles.add(filePath)
            }
            val fileListPath = "${context.cacheDir}/file_list.txt"
            File(fileListPath).printWriter().use { out ->
                if (!isActive) return@withContext false
                convertedFiles.forEach { out.println("file '$it'") }
            }
//            val commandMerge = "-f concat -safe 0 -i \"$fileListPath\" -c copy \"$outputPath\""

//            val commandMerge = "-f concat -safe 0 -i \"$fileListPath\" -c:a libmp3lame \"$outputPath\""

            val filterComplex = convertedFiles.indices.joinToString(" ") { "[$it:a]" } + "concat=n=${convertedFiles.size}:v=0:a=1[outa]"
            val commandMerge = convertedFiles.joinToString(" ") { "-i \"$it\"" } + " -filter_complex \"$filterComplex\" -map \"[outa]\" \"$outputPath\""

////            val filterComplex = convertedFiles.indices.joinToString(" ") { "[$it:a]" } + "concat=n=${convertedFiles.size}:v=0:a=1[outa]"
//            val commandMerge = convertedFiles.joinToString(" ") { "-i \"$it\"" } + " -filter_complex \"$filterComplex\" -map \"[outa]\" \"$outputPath\""

            val rcMerge = FFmpeg.execute(commandMerge)

            if (rcMerge == 0 && isActive) {
                if (checkDone){
                    val timestamp = System.currentTimeMillis()
                    val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
                    val path = File("${musicDir.absolutePath}/${timestamp}_merger.mp3")
                    val cacheFile = File(outputPath)
                    cacheFile.copyTo(path, overwrite = true)
                    var audioInfoConverter = FileInfo.getFileInfoFromPath(Uri.parse(outputPath).toString())
                    audioInfo = AudioSpeedModel(Uri.fromFile(path),audioInfoConverter!!.duration.toString(),audioInfoConverter.fileSize,audioInfoConverter.fileName.toString())// Copy file sang external storage
//                    cacheFile.delete() // Xóa file cache sau khi di chuyển thành công
                }
                Log.d("check_merger", "Gộp âm thanh thành công.")
                selectTypeAudio = "AudioMerger"
                selectType = ""
                true
            } else {
                val mergerPath = File(MergerUri)
                mergerPath.delete()
                Log.e("FFmpeg", "Lỗi khi gộp âm thanh: $rcMerge")
                false // Trả về false khi gộp thất bại
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initAction() {
        binding.recFileConvert.itemAnimator = null
        adapter.onClickListener(object : MergerAudioAdapter.onClickItemListener{
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onItemClick(position: Int) {
                if(listAudioPickMerger.size == 2){
                    Toast.makeText(this@MergerAudioActivity, getString(R.string.items_cannot_be_deleted_you_need_at_least_2_items_to_convert), Toast.LENGTH_SHORT).show()
                }else {
                    if(mediaPlayer_1!=null){
                        mediaPlayer_1?.release()
                        mediaPlayer_1 = null
                    }
                    if(checkMerger == false){
                        mediaPlayer?.pause()
                        mediaPlayer?.seekTo(0)
                    }
                    binding.seekBarAudio.progress = 0
                    binding.tvTimeStart.text = "00:00"
                    binding.tvDuration.text = "/ 00:00"
                    binding.imvPause.visibility = View.GONE
                    binding.imvPlay.visibility = View.VISIBLE
                    checkMerger = true
                    isPlaying = false
                    clearCache()
                    val pos = listAudioPickMerger[position].pos
                    Log.d("check_sizze_audio_pick", "onItemClick: " + pos + "   "+ listAudioPick)
                    if (selectTypeAudio.equals("AudioMerger")){
                        countAudio -= 1
                        countSize -= listAudio[pos].sizeInMB.toInt()
                        listAudioPick.removeAt(position)
                        countMap.clear()
                        for (audio in listAudioPick) {
                            countMap[audio.name] = countMap.getOrDefault(audio.name, 0) + 1
                        }
                        elementCounts = countMap.map { (name, count) ->
                            ElementCount(name, count)
                        }.toMutableList()
                        if(!listAudioPick.contains(listAudio[pos])){
                            Log.d("check_pickckck", "onItemClick: okeeee")
                            listAudio[pos].active = false
                        }
                        if(listAudioPick.size>1){
                            binding.tvTitle.text = "${listAudioPick.size} audio files"
                        }
                    }
                    listAudioPickMerger.removeAt(position)
                    listAudioPickMerger.forEach { it.activePl = false }
                    adapter.notifyDataSetChanged()
                }
            }
            override fun onClickPlay(position: Int, holder: MergerAudioAdapter.ViewHolder) {
                binding.seekBarAudio.isEnabled = false
                adapter.notifyDataSetChanged()
                Log.d("cjcjcjcc", "onClickPlay: ")
                val previouslySelectedIndex = listAudioPickMerger.indexOfFirst { it.activePl }
                if (previouslySelectedIndex != -1 && previouslySelectedIndex != position) {
                    listAudioPickMerger[previouslySelectedIndex].activePl = false
                    adapter.notifyItemChanged(previouslySelectedIndex)
                    if (mediaPlayer != null) {
                        mediaPlayer_1!!.release()
                        mediaPlayer_1 = null
                    }
                }
                if (!listAudioPickMerger[position].activePl) {
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
                    if (mediaPlayer!=null){
                        pausePlaying()
                        binding.imvPause.visibility = View.GONE
                        binding.imvPlay.visibility = View.VISIBLE
                    }
                    listAudioPickMerger[position].activePl = true
                    mediaPlayer_1 = setupMediaPlayer(this@MergerAudioActivity, listAudioPickMerger[position].uri)
                    if(mediaPlayer_1!= null){
                        mediaPlayer_1!!.start()
                    }

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
                    listAudioPickMerger[position].activePl = false
                    mediaPlayer_1?.pause()
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
            checkDone = false
            countPlay += 1
            listAudioPickMerger.forEach { it.activePl = false }
            adapter.notifyDataSetChanged()
            if(mediaPlayer_1!=null){
                mediaPlayer_1!!.release()
                mediaPlayer_1 = null
            }
            if(checkMerger == true) {
                binding.progress.visibility = View.VISIBLE
                val timestamp = System.currentTimeMillis()
                MergerUri = "${cacheDir.path}/merger_cache_${timestamp}.mp3"
                Log.d("check_click", "initAction: thanhhhh anjanah")
                    job = CoroutineScope(Dispatchers.Main).launch{
                        Log.d("check_audio_link", "initData3: "+ listAudioPick)
                        val isMergedSuccessfully = mergeAudioFilesTemp(this@MergerAudioActivity, MergerUri)
                        if (isMergedSuccessfully) {
                            binding.seekBarAudio.isEnabled = true
                            Log.d("check_click", "cutAudio: "+ MergerUri)
                            binding.progress.visibility = View.GONE
                            createMediaPlayer()
                            startPlaying()
                            updateTimeAndSeekBar()
                            binding.seekBarAudio.max = mediaPlayer!!.duration
                            playVideo()
                            binding.imvPause.visibility = View.VISIBLE
                            binding.imvPlay.visibility = View.GONE
                            checkMerger = false
                        }
                    }
                }else{
                    binding.imvPause.visibility = View.VISIBLE
                    binding.imvPlay.visibility = View.GONE
                    updateTimeAndSeekBar()
                    startPlaying()
            }
        }
        binding.progress.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                job?.cancel()
                FFmpeg.cancel()
                binding.progress.visibility = View.GONE
            }
            true
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
            checkDone = true
            handler.removeCallbacksAndMessages(null)
            Log.d("check_audio_link", "initData1: "+ listAudioPick)
            if(listAudioPick.size ==1 ){
                Toast.makeText(this, getString(R.string.you_must_choose_2_or_more_items), Toast.LENGTH_SHORT).show()
            }else{
                if (mediaPlayer != null){
                    mediaPlayer?.pause()
                    isPlaying = false
                    binding.imvPause.visibility = View.GONE
                    binding.imvPause.visibility = View.VISIBLE
                }
                if(mediaPlayer_1!=null){
                    mediaPlayer_1?.pause()
                    mediaPlayer_1?.seekTo(0)
                }
                showLoadingOverlay()
                val timestamp = System.currentTimeMillis()
//                val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
//                outputPath = "${musicDir.absolutePath}/${timestamp}_merger.mp3"
                outputPath = "${cacheDir.path}/merger_${timestamp}.mp3"
                Log.d("check_audio_link", "initData2: "+ listAudioPick)
                job = CoroutineScope(Dispatchers.Main).launch{
                    Log.d("check_audio_link", "initData3: "+ listAudioPick)
                    val isMergedSuccessfully = mergeAudioFilesTemp(this@MergerAudioActivity, outputPath)
                    if (isMergedSuccessfully) {
                        if(countPlay == 0){
                            checkMerger = true
                        }else{
                            checkMerger = false
                        }
                        hideLoadingOverlay()
                        startActivity(Intent(this@MergerAudioActivity,SavedActivity::class.java))
                    }else{
                        clearCache()
                    }
                }
            }
        }
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

    private fun playVideo() {
//        runOnUiThread(object : Runnable {
//            override fun run() {
//                    if(mediaPlayer!=null){
//                        val currentPosition = mediaPlayer.currentPosition
//                        binding.seekBarAudio.progress = currentPosition
//                    }
//                handler.postDelayed(this, 100) // Cập nhật SeekBar mỗi giây
//            }
//        })
        binding.tvDuration.text = "/ ${formatTimeToHoursMinutes(mediaPlayer!!.duration)}"
        mediaPlayer!!.setOnCompletionListener {
            binding.tvTimeStart.text = formatTimeToHoursMinutes(mediaPlayer!!.duration)
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
        if(mediaPlayer != null){
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
        adapter.getData(listAudioPickMerger)
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
                    Collections.swap(listAudioPickMerger, from, to)
                    binding.seekBarAudio.isEnabled = false
                    if(checkMerger == false){
                        mediaPlayer?.pause()
                        mediaPlayer?.seekTo(0)
                    }
                    if(mediaPlayer_1!= null){
                        listAudioPickMerger.forEach { it.activePl = false }
                        mediaPlayer_1!!.release()
                    }
                    binding.seekBarAudio.isClickable = false
                    binding.seekBarAudio.progress = 0
                    binding.tvTimeStart.text = "00:00"
                    binding.tvDuration.text = "/ 00:00"
                    binding.imvPause.visibility = View.GONE
                    binding.imvPlay.visibility = View.VISIBLE
                    checkMerger = true
                    isPlaying = false
                    handler.removeCallbacksAndMessages(null)
                    adapter?.notifyItemMoved(from, to)
                    return true
                }
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                }
                override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                    super.clearView(recyclerView, viewHolder)
                    adapter.notifyDataSetChanged()
                }
            }
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.recFileConvert)
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun showLoadingOverlay() {
        binding.loadingOverlay.visibility = View.VISIBLE
        val animator = ObjectAnimator.ofInt(binding.lottieAnimationView, "progress", 0, 100)
        animator.duration = 3000 // Thời gian chạy animation (2 giây)
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

    fun setupMediaPlayer(context: Context, uri: Uri): MediaPlayer? {
        // Tạo đối tượng MediaPlayer
        val mediaPlayer = MediaPlayer()
        try {
            // Thiết lập nguồn nhạc từ URI
            mediaPlayer.setDataSource(context, uri)
            // Chuẩn bị phát nhạc
            mediaPlayer.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
            return null // Trả về null nếu có lỗi
        }
        // Bắt đầu phát nhạc
        mediaPlayer.start()

        // Trả về đối tượng MediaPlayer
        return mediaPlayer
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
//        if(mediaPlayer != null){
//            mediaPlayer!!.release()
//        }
        if(mediaPlayer_1!= null){
            mediaPlayer_1!!.release()
        }
        job?.cancel()
        hideLoadingOverlay()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onResume() {
        super.onResume()
//        if(binding.progress.visibility == View.VISIBLE){
//            checkDone = false
//            val timestamp = System.currentTimeMillis()
//            MergerUri = "${cacheDir.path}/merger_cache_${timestamp}.mp3"
//            Log.d("check_click", "initAction: thanhhhh anjanah")
//            job = CoroutineScope(Dispatchers.Main).launch{
//                Log.d("check_audio_link", "initData3: "+ listAudioPick)
//                val isMergedSuccessfully = mergeAudioFilesTemp(this@MergerAudioActivity, MergerUri)
//                if (isMergedSuccessfully) {
//                    binding.seekBarAudio.isEnabled = true
//                    Log.d("check_click", "cutAudio: "+ MergerUri)
//                    binding.progress.visibility = View.GONE
//                    createMediaPlayer()
//                    startPlaying()
//                    updateTimeAndSeekBar()
//                    binding.seekBarAudio.max = mediaPlayer!!.duration
//                    playVideo()
//                    binding.imvPause.visibility = View.VISIBLE
//                    binding.imvPlay.visibility = View.GONE
//                    checkMerger = false
//                }
//            }
//        }else if(binding.loadingOverlay.visibility == View.VISIBLE){
//            checkDone = true
//            val timestamp = System.currentTimeMillis()
////            val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
//            outputPath = "${cacheDir.path}/merger_${timestamp}.mp3"
//            Log.d("check_audio_link", "initData2: "+ listAudioPick)
//            job = CoroutineScope(Dispatchers.Main).launch{
//                Log.d("check_audio_link", "initData3: "+ listAudioPick)
//                val isMergedSuccessfully = mergeAudioFilesTemp(this@MergerAudioActivity, outputPath)
//                if (isMergedSuccessfully) {
//                    checkMerger = false
//                    var audioInfoConverter = FileInfo.getFileInfoFromPath(Uri.parse(outputPath).toString())
//                    audioInfo = AudioSpeedModel(Uri.parse(outputPath),audioInfoConverter!!.duration.toString(),audioInfoConverter.fileSize,audioInfoConverter.fileName.toString())
//                    startActivity(Intent(this@MergerAudioActivity,SavedActivity::class.java))
//                }else{
//                    clearCache()
//                }
//            }
//        }else{
            binding.imvPause.visibility = View.GONE
            binding.imvPlay.visibility = View.VISIBLE
            clearCache()
//        }
    }

    override fun onStop() {
        super.onStop()
        job?.cancel()
//        FFmpeg.cancel()
        isPlaying = false
        if (mediaPlayer != null){
            mediaPlayer?.pause()
        }
        if(mediaPlayer_1!= null){
            mediaPlayer_1!!.pause()
            listAudioPickMerger.forEach { it.activePl = false }
            adapter.notifyDataSetChanged()
        }
        handler.removeCallbacksAndMessages(null)
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if(binding.loadingOverlay.visibility == View.VISIBLE){
            listAudioPickMerger.forEach { it.activePl = false }
            adapter.notifyDataSetChanged()
            hideLoadingOverlay()
            startCoroutine()
        }else{
            finish()
        }
    }

    fun startCoroutine() {
        job?.cancel()
        FFmpeg.cancel()
        clearCache()
        audioInfo = null
        binding.imvPause.visibility = View.GONE
        binding.imvPlay.visibility = View.VISIBLE
    }

}