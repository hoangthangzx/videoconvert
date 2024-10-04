package com.kan.dev.st_042_video_to_mp3.ui.file_convert_to_mp3

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.arthenica.mobileffmpeg.FFmpeg
//import com.arthenica.mobileffmpeg.FFmpeg
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivityFileConvertToMp3Binding
import com.kan.dev.st_042_video_to_mp3.ui.SavedActivity
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSizeVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.countVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.mp3Uri
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionVideoPlay
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.AbsBaseActivity
import java.io.File

class FileConvertToMp3Activity : AbsBaseActivity<ActivityFileConvertToMp3Binding>(false) {
    override fun getFragmentID(): Int = 0
    override fun getLayoutId(): Int  = R.layout.activity_file_convert_to_mp3
    lateinit var adapter: FileConvertAdapter
    private var exoPlayer: ExoPlayer? = null
    var videoUri : Uri? = null
    override fun init() {
        initData()
        initAction()
        initView()
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
        if(listVideo.size>0){
            videoUri = Uri.parse(listVideo[positionVideoPlay].uri.toString())
            Log.d("check_mp3", "initData: "+ videoUri)
        }
    }
    private fun initView() {

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
            exoPlayer!!.pause()
            binding.ctlProgress.visibility = View.VISIBLE
            if(listVideoPick.size == 1){
                val videoPath = getRealPathFromURI(this,videoUri!!)
                val timestamp = System.currentTimeMillis()
                val musicDir = File(Environment.getExternalStorageDirectory(), "Music/music")
                val outputPath = "${musicDir.absolutePath}/${File(videoPath).name.substringBeforeLast(".") }_${timestamp}_convert.mp3"
                if (videoPath != null) {
                    convertVideoToMp3(videoPath, outputPath)
                }
                mp3Uri = Uri.parse(outputPath)
            }else{
                Toast.makeText(this, "", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun convertVideoToMp3(videoUri: String, outputPath: String) {
        val command = "-i $videoUri -vn -ar 44100 -ac 2 -b:a 192k $outputPath"
        val resultCode = FFmpeg.execute(command)
        if (resultCode == 0) {
            binding.ctlProgress.visibility = View.GONE
            startActivity(Intent(this@FileConvertToMp3Activity,SavedActivity::class.java))
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
    }

    override fun onStop() {
        super.onStop()
        exoPlayer?.release()
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
}