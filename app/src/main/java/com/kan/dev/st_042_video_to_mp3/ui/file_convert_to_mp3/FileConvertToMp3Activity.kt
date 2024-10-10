package com.kan.dev.st_042_video_to_mp3.ui.file_convert_to_mp3

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.arthenica.mobileffmpeg.FFmpeg
//import com.arthenica.mobileffmpeg.FFmpeg
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivityFileConvertToMp3Binding
import com.kan.dev.st_042_video_to_mp3.model.AudioSpeedModel
import com.kan.dev.st_042_video_to_mp3.model.VideoConvertModel
import com.kan.dev.st_042_video_to_mp3.model.VideoCutterModel
import com.kan.dev.st_042_video_to_mp3.ui.saved.SavedActivity
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.Const.audioInfo
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSizeVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.countVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioSaved
import com.kan.dev.st_042_video_to_mp3.utils.Const.listConvertMp3
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.mp3Uri
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionVideoPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectType
import com.kan.dev.st_042_video_to_mp3.utils.Const.videoCutter
import com.kan.dev.st_042_video_to_mp3.utils.FileInfo
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.AbsBaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FileConvertToMp3Activity : AbsBaseActivity<ActivityFileConvertToMp3Binding>(false) {
    override fun getFragmentID(): Int = 0
    override fun getLayoutId(): Int  = R.layout.activity_file_convert_to_mp3
    lateinit var adapter: FileConvertAdapter
    private var exoPlayer: ExoPlayer? = null
    var videoUri : Uri? = null
    var videoPath : String? = null
    var count = 0
    private var isConverting = false
    override fun init() {
        initData()
        initAction()
        if(listVideoPick.size == 1){
            initViewFile()
            initDataFile()
        }else{
            initViewMutil()
            initDataMulti()
            initActionMulti()
        }
    }

    private fun initData() {
        if(selectType.equals("VideoCutter")){
            videoUri = videoCutter!!.uri
            Log.d("check_kfkfjfkf", "initData: "+ videoUri)
        }
        else if(listVideo.size>0 && selectType.equals("Video")){
            videoUri = Uri.parse(listVideo[positionVideoPlay].uri.toString())
            Log.d("check_mp3", "initData: "+ videoUri)
        }
    }

    private fun initDataFile() {
        Log.d("check_file", "initDataFile: "+ listVideo[positionVideoPlay])
        exoPlayer = ExoPlayer.Builder(this).build()
        binding.exoVideo.player = exoPlayer
        val mediaItem = MediaItem.fromUri(videoUri!!)
        exoPlayer!!.setMediaItem(mediaItem)
        exoPlayer!!.prepare()
        exoPlayer!!.playWhenReady = true
    }

    private fun initActionMulti() {
        adapter.onClickListener(object : FileConvertAdapter.onClickItemListener{
            override fun onItemClick(position: Int) {
                val pos = listVideoPick[position].pos
                listVideo[pos].active = false
                countVideo -= 1
                countSizeVideo -= listVideo[pos].sizeInMB.toInt()
                listVideoPick.removeAt(position)
                adapter.notifyDataSetChanged()
            }
        })
    }

    private fun initAction() {
        binding.imvBack.onSingleClick {
            finish()
        }
        binding.LnConvert.onSingleClick {
            if(listVideoPick.size == 1){
                if(exoPlayer!=null){
                    exoPlayer!!.pause()
                }
                if(selectType.equals("VideoCutter")){
                    videoPath = videoCutter!!.uri.toString()
                    Log.d("check_path", "initAcrjngrgrgtion: "+ videoPath)
                    selectType = "VideoCutterToMp3"
                }else{
                    videoPath = getRealPathFromURI(this,videoUri!!)
                }
                showLoadingOverlay()
                val timestamp = System.currentTimeMillis()
                val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
                Log.d("check_path", "initAction: "+ videoPath)
                val outputPath = "${musicDir.absolutePath}/${File(videoPath).name.substringBeforeLast(".") }_${timestamp}_convert.mp3"
                if (videoPath != null) {
                    convertVideoToMp3(videoPath!!, outputPath)
                }
            }else{
                if (!isConverting) {
                    showLoadingOverlay()
                    isConverting = true
                    CoroutineScope(Dispatchers.Main).launch {
                        convertAllVideosToMp3()
                    }
                }
            }
        }
    }

    private suspend fun convertAllVideosToMp3() {
        withContext(Dispatchers.IO) { // Chạy trong IO context
            for(video in listVideoPick){
                val videoPath = getRealPathFromURI(this@FileConvertToMp3Activity,video.uri)
                val timestamp = System.currentTimeMillis()
                val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
                val outputPath = "${musicDir.absolutePath}/${File(videoPath).name.substringBeforeLast(".") }_${timestamp}_convert.mp3"
                if (videoPath != null) {
                    convertVideoToMp3(videoPath, outputPath)
                }
            }
        }
        startActivity(Intent(this, SavedActivity::class.java))
        isConverting = false
    }


    private fun showLoadingOverlay() {
        binding.loadingOverlay.visibility = View.VISIBLE
        val animator = ObjectAnimator.ofInt(binding.lottieAnimationView, "progress", 0, 100)
        animator.duration = 1000 // Thời gian chạy animation (5 giây)
        animator.start()
    }

    private fun hideLoadingOverlay() {
        binding.loadingOverlay.visibility = View.GONE
    }

    fun convertVideoToMp3(videoUri: String, outputPath: String) {
        val command = "-i \"$videoUri\" -vn -ar 44100 -ac 2 -b:a 192k $outputPath"
        val resultCode = FFmpeg.execute(command)
        if (resultCode == 0) {
            if (listVideoPick.size == 1){
                mp3Uri = Uri.parse(outputPath)
                val infoFile = FileInfo.getFileInfoFromPath(mp3Uri!!.toString())
                Const.videoConvert = VideoConvertModel(mp3Uri!!, videoCutter!!.duration,infoFile!!.fileSize,infoFile.fileName.toString() )
                startActivity(Intent(this@FileConvertToMp3Activity,SavedActivity::class.java))
            }else{
                var audioInfoConverter = FileInfo.getFileInfoFromPath(Uri.parse(outputPath).toString())
                audioInfo = AudioSpeedModel(Uri.parse(outputPath),audioInfoConverter!!.duration.toString(),audioInfoConverter.fileSize,audioInfoConverter.fileName.toString())
                listAudioSaved.add(audioInfo!!)
            }
            listConvertMp3.add(outputPath)
        } else {
            Log.d("check_mp3", "Chuyển đổi thất bại. Mã lỗi: $resultCode")
        }
    }
    private fun initViewFile() {
        binding.exoVideo.visibility = View.VISIBLE
        binding.tvTitle.text = getString(R.string.convert_to_mp3)
    }

    private fun initViewMutil() {
        binding.tvTitle.text = getString(R.string.multifile_convert)
        binding.recFileConvert.visibility = View.VISIBLE
    }

    private fun initDataMulti() {
        adapter = FileConvertAdapter(this@FileConvertToMp3Activity)
        adapter.getData(listVideoPick)
        binding.recFileConvert.adapter = adapter
        Log.d("check_list_video_pick", "initDataMulti: " + listVideoPick)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (exoPlayer?.isPlaying == true) {
            exoPlayer?.pause() // Dừng phát âm thanh nếu đang phát
            exoPlayer?.release() // Giải phóng tài nguyên
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

    override fun onResume() {
        super.onResume()
        hideLoadingOverlay()
        initData()
    }
}