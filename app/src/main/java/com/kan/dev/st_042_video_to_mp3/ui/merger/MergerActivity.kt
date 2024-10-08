//package com.kan.dev.st_042_video_to_mp3.ui.merger
//
//import android.content.Context
//import android.database.Cursor
//import android.net.Uri
//import android.os.Environment
//import android.provider.MediaStore
//import android.util.Log
//import android.view.View
//import android.widget.ImageView
//import android.widget.LinearLayout
//import androidx.core.content.ContextCompat
//import androidx.media3.common.MediaItem
//import androidx.media3.exoplayer.ExoPlayer
//import androidx.recyclerview.widget.ItemTouchHelper
//import androidx.recyclerview.widget.ItemTouchHelper.DOWN
//import androidx.recyclerview.widget.ItemTouchHelper.END
//import androidx.recyclerview.widget.ItemTouchHelper.START
//import androidx.recyclerview.widget.ItemTouchHelper.UP
//import androidx.recyclerview.widget.RecyclerView
//import com.arthenica.mobileffmpeg.FFmpeg
//import com.bumptech.glide.Glide
//import com.kan.dev.st_042_video_to_mp3.R
//import com.kan.dev.st_042_video_to_mp3.databinding.ActivityMergerVideoBinding
//import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoPick
//import com.kan.dev.st_042_video_to_mp3.utils.applyGradient
//import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
//import com.metaldetector.golddetector.finder.AbsBaseActivity
//import java.io.File
//import java.lang.reflect.Type
//import java.util.Collections
//
//class MergerActivity : AbsBaseActivity<ActivityMergerVideoBinding>(false) {
//    override fun getFragmentID(): Int  = 0
//    override fun getLayoutId(): Int = R.layout.activity_merger_video
//    var selectedLayout: LinearLayout? = null
//    var selectedIcon: ImageView? = null
//    private var exoPlayer: ExoPlayer? = null
//    var videoUri : Uri? = null
//    var posPrePlay = 0
//    var TypeMerger = "Sq"
//    lateinit var adapter: MultiFileAdapter
//    override fun init() {
//        initData()
//        initView()
//        initAction()
//    }
//
//    private fun initData() {
//        onLayoutClick(binding.lnSequence, binding.imvBoxSq)
//        videoUri = listVideoPick[0].uri
//        adapter = MultiFileAdapter(this)
//        adapter.getData(listVideoPick)
//        binding.recVideoMerger.adapter = adapter
//
//        val simpleItemTouchCallback =
//            object : ItemTouchHelper.SimpleCallback(
//                UP or DOWN or START or END,
//                0) {
//                override fun onMove(
//                    recyclerView: RecyclerView,
//                    viewHolder: RecyclerView.ViewHolder,
//                    target: RecyclerView.ViewHolder): Boolean {
//                    // [3] Do something when an item is moved
//
//                    val adapter = recyclerView.adapter
//                    val from = viewHolder.adapterPosition
//                    val to = target.adapterPosition
//                    Collections.swap(listVideoPick, from, to)
//                    adapter?.notifyItemMoved(from, to)
//                    adapter?.notifyDataSetChanged()
//                    return true
//                }
//
//                override fun onSwiped(
//                    viewHolder: RecyclerView.ViewHolder,
//                    direction: Int) {
//                }
//
//                override fun clearView(
//                    recyclerView: RecyclerView,
//                    viewHolder: RecyclerView.ViewHolder) {
//                    super.clearView(recyclerView, viewHolder);
//                }
//            }
//
//        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
//        itemTouchHelper.attachToRecyclerView(binding.recVideoMerger)
//        playExoPlayer()
//    }
//
//    private fun playExoPlayer() {
//        exoPlayer = ExoPlayer.Builder(this).build()
//        binding.exoVideo.player = exoPlayer
//        val mediaItem = MediaItem.fromUri(videoUri!!)
//        exoPlayer!!.setMediaItem(mediaItem)
//        exoPlayer!!.prepare()
//        exoPlayer!!.playWhenReady = true
//    }
//
//    private fun initView() {
//
//        binding.tvSq.isSelected = true
//        binding.tvLR.isSelected = true
//        binding.tvUD.isSelected = true
//
//        val colors = intArrayOf(
//            ContextCompat.getColor(this@MergerActivity, R.color.color_1),
//            ContextCompat.getColor(this@MergerActivity, R.color.color_2)
//        )
//        binding.tvSave.applyGradient(this@MergerActivity,colors)
//    }
//
//    private fun initAction() {
//        binding.imvBack.onSingleClick {
//            finish()
//        }
//
//        binding.tvSave.onSingleClick {
//            val videoPath1 = getRealPathFromURI(this, listVideoPick[0].uri)
//            val videoPath2 = getRealPathFromURI(this, listVideoPick[1].uri)
//            val timestamp = System.currentTimeMillis()
//            val musicDir = File(Environment.getExternalStorageDirectory(), "Movies/video")
//            val outputPath = "${musicDir.absolutePath}/${File(videoPath1.toString()).name.substringBeforeLast(".") }_${timestamp}_merger.mp4"
//            if(TypeMerger.equals("Lr")){
//                mergeVideos(videoPath1.toString(), videoPath2.toString(),outputPath)
//            } else if (TypeMerger.equals("Sq")){
//                mergeVideosSq(videoPath1.toString(), videoPath2.toString(),outputPath)
//            }else{
//                mergeVideosVertically(videoPath1.toString(), videoPath2.toString(),outputPath)
//            }
//
//        }
//
//        adapter.onClickListener(object : MultiFileAdapter.onClickItemListener{
//            override fun onItemClick(position: Int, holder: MultiFileAdapter.ViewHolder) {
//                exoPlayer!!.release()
//                listVideoPick[posPrePlay].active = true
//                posPrePlay = position
//                videoUri = listVideoPick[position].uri
//                listVideoPick[position].active = false
//                adapter.notifyDataSetChanged()
//                playExoPlayer()
//            }
//        })
//
//        binding.lnMerger.onSingleClick {
//            binding.imvMulti.visibility = View.GONE
//            binding.imvSound.visibility = View.GONE
//            binding.imvMerger.visibility = View.VISIBLE
//            binding.tvMulti.visibility = View.VISIBLE
//            binding.tvSound.visibility = View.VISIBLE
//            binding.tvMerger.visibility = View.GONE
//            binding.lnMergerStyle.visibility = View.VISIBLE
//            binding.recVideoMerger.visibility = View.GONE
//        }
//
//        binding.lnMedia.onSingleClick {
//            binding.lnVideoLR.visibility = View.GONE
//            binding.lnVideoUD.visibility = View.GONE
//            binding.exoVideo.visibility = View.VISIBLE
//            binding.imvMulti.visibility = View.VISIBLE
//            binding.imvSound.visibility = View.GONE
//            binding.imvMerger.visibility = View.GONE
//            binding.tvMulti.visibility = View.GONE
//            binding.tvSound.visibility = View.VISIBLE
//            binding.tvMerger.visibility = View.VISIBLE
//            binding.lnMergerStyle.visibility = View.GONE
//            binding.recVideoMerger.visibility = View.VISIBLE
//        }
//
//        binding.lnSound.onSingleClick{
//            binding.imvMulti.visibility = View.GONE
//            binding.imvSound.visibility = View.VISIBLE
//            binding.imvMerger.visibility = View.GONE
//            binding.tvMulti.visibility = View.VISIBLE
//            binding.tvSound.visibility = View.GONE
//            binding.tvMerger.visibility = View.VISIBLE
//        }
//
//        binding.lnSequence.onSingleClick {
//            TypeMerger = "Sq"
//            onLayoutClick(binding.lnSequence, binding.imvBoxSq)
//            binding.exoVideo.visibility = View.VISIBLE
//            binding.lnVideoLR.visibility = View.GONE
//            binding.lnVideoUD.visibility = View.GONE
//        }
//
//        binding.lnLeftAndRight.onSingleClick {
//            exoPlayer!!.pause()
//            TypeMerger = "Lr"
//            onLayoutClick(binding.lnLeftAndRight, binding.imvBoxLR)
//            binding.exoVideo.visibility = View.GONE
//            binding.lnVideoLR.visibility = View.VISIBLE
//            binding.lnVideoUD.visibility = View.GONE
//            Glide.with(this)
//                .asBitmap()
//                .load(listVideoPick[0].uri)
//                .into(binding.imvVideo1)
//            Glide.with(this)
//                .asBitmap()
//                .load(listVideoPick[1].uri)
//                .into(binding.imvVideo2)
//        }
//
//        binding.lnUpAndDown.onSingleClick {
//            TypeMerger = "Ud"
//            onLayoutClick(binding.lnUpAndDown, binding.imvBoxUD)
//            binding.exoVideo.visibility = View.GONE
//            binding.lnVideoLR.visibility = View.GONE
//            binding.lnVideoUD.visibility = View.VISIBLE
//            Glide.with(this)
//                .asBitmap()
//                .load(listVideoPick[0].uri)
//                .into(binding.imvVideo1UD)
//            Glide.with(this)
//                .asBitmap()
//                .load(listVideoPick[1].uri)
//                .into(binding.imvVideo2UD)
//        }
//    }
//
//    fun onLayoutClick(clickedLayout: LinearLayout, clickedIcon: ImageView) {
//        selectedLayout?.isSelected = false
//        selectedIcon?.isSelected = false
//        clickedLayout.isSelected = true
//        clickedIcon.isSelected = true
//        selectedLayout = clickedLayout
//        selectedIcon = clickedIcon
//    }
//
//    fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
//        var path: String? = null
//        val proj = arrayOf(MediaStore.Video.Media.DATA)
//        val cursor: Cursor? = context.contentResolver.query(contentUri, proj, null, null, null)
//        cursor?.use {
//            if (it.moveToFirst()) {
//                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
//                path = it.getString(columnIndex)
//            }
//        }
//        return path
//    }
//
//    fun mergeVideos(videoPath1: String, videoPath2: String, outputPath: String) {
//        val command = arrayOf(
//            "-y",  // Ghi đè nếu tệp đầu ra đã tồn tại
//            "-i", videoPath1,  // Video đầu tiên
//            "-i", videoPath2,  // Video thứ hai
//            "-filter_complex", "[0:v]scale=width:height[v0];[1:v]scale=width:height[v1];[v0][v1]hstack=inputs=2[outv]",  // Nối video theo hàng ngang
//            "-map", "[outv]",  // Lấy kết quả video đã nối
//            outputPath  // Đường dẫn video đầu ra
//        )
////        val returnCode2 = session2.returnCode
//        // Thực thi lệnh
//        val result = FFmpeg.execute(command)
//
//        if (result == 0) {
//            // Thành công
//            Log.d("check_kq", "mergeVideos: okeeeeeeeeeeee")
//        } else { // Lấy thông báo lỗi
//            Log.d("check_kq", "mergeVideos: khong duoc. Lý do")
//        }
//
//    }
//
//    fun mergeVideosSq(videoPath1: String, videoPath2: String, outputPath: String) {
//        val command = arrayOf(
//            "-y",  // Ghi đè nếu tệp đầu ra đã tồn tại
//            "-i", videoPath1,  // Video đầu tiên
//            "-i", videoPath2,  // Video thứ hai
//            "-filter_complex", "[0:v][0:a][1:v][1:a]concat=n=2:v=1:a=1[outv][outa]",  // Nối video và audio theo thứ tự
//            "-map", "[outv]",  // Map video đầu ra
//            "-map", "[outa]",  // Map audio đầu ra
//            outputPath  // Đường dẫn video đầu ra
//        )
//        val result = FFmpeg.execute(command)
//        if (result == 0) {
//            // Thành công
//            Log.d("check_kq", "mergeVideos: thanh cong")
//        } else {
//            // Thất bại
//            Log.d("check_kq", "mergeVideos: that bai")
//        }
//
//    }
//
//    fun mergeVideosVertically(videoPath1: String, videoPath2: String, outputPath: String) {
//        val command = arrayOf(
//            "-y",
//            "-i", videoPath1,
//            "-i", videoPath2,
//            "-filter_complex", "[0:v][1:v]vstack=inputs=2[outv]",
//            "-map", "[outv]",
//            outputPath
//        )
//
//        val result = FFmpeg.execute(command)
//
//        if (result == 0) {
//            // Thành công
//            Log.d("check_kq", "mergeVideos: okeeeeeeeeeeee")
//
//        } else {
//            // Thất bại
//            Log.d("check_kq", "mergeVideos: Nooooooooo")
//        }
//    }
//}