package com.kan.dev.st_042_video_to_mp3.ui.select_video

import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivitySelectVideoBinding
import com.kan.dev.st_042_video_to_mp3.model.VideoInfo
import com.kan.dev.st_042_video_to_mp3.ui.VideoConverterActivity
//import com.kan.dev.st_042_video_to_mp3.ui.merger.MergerActivity
import com.kan.dev.st_042_video_to_mp3.ui.VideoCutterActivity
import com.kan.dev.st_042_video_to_mp3.ui.VideoSpeedActivity
import com.kan.dev.st_042_video_to_mp3.ui.file_convert_to_mp3.FileConvertToMp3Activity
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.Const.audioInfo
import com.kan.dev.st_042_video_to_mp3.utils.Const.checkData
import com.kan.dev.st_042_video_to_mp3.utils.Const.checkType
import com.kan.dev.st_042_video_to_mp3.utils.Const.countAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSize
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSizeVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.countVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioSaved
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioStorage
import com.kan.dev.st_042_video_to_mp3.utils.Const.listConvertMp3
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoF
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoT
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionVideoPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectType
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectTypeAudio
import com.kan.dev.st_042_video_to_mp3.utils.VideoUtils
import com.kan.dev.st_042_video_to_mp3.utils.applyGradient
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.AbsBaseActivity

class SelectVideoActivity : AbsBaseActivity<ActivitySelectVideoBinding>(false) {
    override fun getFragmentID(): Int = 0
    override fun getLayoutId(): Int = R.layout.activity_select_video
    lateinit var adapter: SelectVideoAdapter
    var isAll = false
    override fun init() {
        if(listVideo.size == 0){
            binding.noItem.visibility = View.VISIBLE
        }
        initData()
        initView()
        if(selectType.equals("VideoCutter")){
            initActionSpeed()
//            binding.imvTick.visibility = View.INVISIBLE
        }else if(selectType.equals("VideoConvert")){
            initActionConverter()
        }else{
            initAction()
        }
    }

    private fun initActionConverter() {
        binding.imvBack.onSingleClick {
            finish()
        }

        binding.lnContinue.onSingleClick {
            if(listVideoPick.size > 0){
                startActivity(Intent(this@SelectVideoActivity, VideoConverterActivity::class.java))
            }else{
                Toast.makeText(this@SelectVideoActivity, getString(R.string.you_must_choose_1_file), Toast.LENGTH_SHORT).show()
            }
        }
        adapter.onClickListener(object : SelectVideoAdapter.onClickItemListener{
            override fun onItemClick(position: Int, holder: SelectVideoAdapter.ViewHolder) {
                if(!listVideo[position].active){
                    positionVideoPlay = position
                    countVideo += 1
                    countSizeVideo += listVideo[position].sizeInMB.toInt()
                    holder.binding.imvCheckbox.setImageResource(R.drawable.icon_check_box_yes)
                    listVideoPick.add(0, listVideo[position])
                    listVideo[position].active = true
                }else if(listVideo[position].active){
                    countVideo -= 1
                    holder.binding.imvCheckbox.setImageResource(R.drawable.icon_check_box)
                    listVideo[position].active = false
                    listVideoPick.remove(listVideo[position])
                    countSizeVideo -= listVideo[position].sizeInMB.toInt()
                }
                binding.tvSelected.text = "$countVideo Selected"
                binding.tvSize.text = "/ $countSizeVideo MB"
            }
        })
    }

    private fun initActionMerger() {
        binding.imvBack.onSingleClick {
            finish()

        }
        binding.lnContinue.onSingleClick {
            if(countVideo>0 && selectType.equals("Video")){
                startActivity(Intent(this@SelectVideoActivity, FileConvertToMp3Activity::class.java))
            } else{
                Toast.makeText(this@SelectVideoActivity, getString(R.string.you_must_choose_1_file), Toast.LENGTH_SHORT).show()
            }
        }
        adapter.onClickListener(object : SelectVideoAdapter.onClickItemListener{
            override fun onItemClick(position: Int, holder: SelectVideoAdapter.ViewHolder) {
                if(!listVideo[position].active){
                    if(countVideo>=2){
                        Toast.makeText(this@SelectVideoActivity, getString(R.string.you_can_only_choose_2_items), Toast.LENGTH_SHORT).show()
                    }else{
                        positionVideoPlay = position
                        countVideo += 1
                        countSizeVideo += listVideo[position].sizeInMB.toInt()
                        holder.binding.imvCheckbox.setImageResource(R.drawable.icon_check_box_yes)
                        listVideoPick.add(0, listVideo[position])
                        listVideo[position].active = true
                    }
                }else if(listVideo[position].active){
                    countVideo -= 1
                    holder.binding.imvCheckbox.setImageResource(R.drawable.icon_check_box)
                    listVideo[position].active = false
                    listVideoPick.remove(listVideo[position])
                    countSizeVideo -= listVideo[position].sizeInMB.toInt()
                }
                binding.tvSelected.text = "$countVideo Selected"
                binding.tvSize.text = "/ $countSizeVideo MB"
            }
        })
    }
    private fun initActionSpeed() {
        binding.imvBack.onSingleClick {
            finish()
        }

        adapter.onClickListener(object : SelectVideoAdapter.onClickItemListener{
            override fun onItemClick(position: Int, holder: SelectVideoAdapter.ViewHolder) {
                if(!listVideo[position].active){
                    if(listVideoPick.size != 0){
                        positionVideoPlay = position
                        val pos = listVideoPick[0].pos
                        listVideo[pos].active = false
                        countSizeVideo = listVideo[position].sizeInMB.toInt()
                        listVideo[position].active = true
                        listVideoPick.removeAt(0)
                        listVideoPick.add(0,listVideo[position])
                        adapter.notifyDataSetChanged()
                    }else{
                        positionVideoPlay = position
                        countVideo += 1
                        countSizeVideo = listVideo[position].sizeInMB.toInt()
                        holder.binding.imvCheckbox.setImageResource(R.drawable.icon_check_box_yes)
                        listVideoPick.add(0, listVideo[position])
                        listVideo[position].active = true
                    }
                }else if(listVideo[position].active){
                    countVideo -= 1
                    countSizeVideo = 0
                    holder.binding.imvCheckbox.setImageResource(R.drawable.icon_check_box)
                    listVideo[position].active = false
                    listVideoPick.remove(listVideo[position])
//                    countSizeVideo -= listVideo[position].sizeInMB.toInt()
                }
                binding.tvSelected.text = "$countVideo Selected"
                binding.tvSize.text = "/ $countSizeVideo MB"
            }
        })

        binding.lnContinue.onSingleClick {
            if(listVideoPick.size>0){
                startActivity(Intent(this@SelectVideoActivity, VideoCutterActivity::class.java))
            }else{
                Toast.makeText(this@SelectVideoActivity, getString(R.string.you_must_choose_1_file), Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun initAction() {
        binding.imvBack.onSingleClick {
            finish()
        }
        binding.lnContinue.onSingleClick {
            if(listVideoPick.size>0 && selectType.equals("Video")){
                startActivity(Intent(this@SelectVideoActivity, FileConvertToMp3Activity::class.java))
            }
            else{
                Toast.makeText(this@SelectVideoActivity, getString(R.string.you_must_choose_1_file), Toast.LENGTH_SHORT).show()
            }
        }

        adapter.onClickListener(object : SelectVideoAdapter.onClickItemListener{
            override fun onItemClick(position: Int, holder: SelectVideoAdapter.ViewHolder) {
                if(!listVideo[position].active){
                    positionVideoPlay = position
                    countVideo += 1
                    countSizeVideo += listVideo[position].sizeInMB.toInt()
                    holder.binding.imvCheckbox.setImageResource(R.drawable.icon_check_box_yes)
                    listVideo[position].active = true
                    listVideoPick.add(0,listVideo[position])
                    if (listVideoPick.size == listVideo.size){
                        binding.imvTick.visibility = View.GONE
                        binding.imvTickTrue.visibility = View.VISIBLE
                    }
                }else if(listVideo[position].active){
                    countVideo -= 1
                    holder.binding.imvCheckbox.setImageResource(R.drawable.icon_check_box)
                    Log.d("check_size_list", "onItemClick: "+ listVideoPick  + "        "+ position+  "     "  + listVideo.size)
                    listVideoPick.remove(listVideo[position])
                    binding.imvTick.visibility = View.VISIBLE
                    binding.imvTickTrue.visibility = View.GONE
                    listVideo[position].active = false
                    Log.d("chejjc-wd-d-e", "onItemClick: "+  listVideo[position])
                    countSizeVideo -= listVideo[position].sizeInMB.toInt()
                }
                binding.tvSelected.text = "$countVideo Selected"
                binding.tvSize.text = "/ $countSizeVideo MB"
            }
        })

        binding.imvTick.onSingleClick {
            isAll = true
            Log.d("check_visible", "initAction:eegfeger ")
            binding.imvTickTrue.visibility = View.VISIBLE
            binding.imvTick.visibility = View.GONE
            listVideo.forEach { it.active = true }
            adapter.notifyDataSetChanged()
            listVideoPick = listVideo.map { it.copy() }.toMutableList()
            countVideo = listVideoPick.size
            countSizeVideo = listVideoPick.sumOf { it.sizeInMB.toInt() }
            binding.tvSelected.text = "$countVideo Selected"
            binding.tvSize.text = "/ $countSizeVideo MB"
        }

        binding.imvTickTrue.onSingleClick {
            countVideo = 0
            countSizeVideo = 0
            binding.tvSelected.text = "$countVideo Selected"
            binding.tvSize.text = "/ $countSizeVideo MB"
            isAll = false
            binding.imvTickTrue.visibility = View.GONE
            binding.imvTick.visibility = View.VISIBLE
            listVideo.forEach { it.active = false }
            Log.d("check_list_video", "initAction: "+ listVideo)
            adapter.notifyDataSetChanged()
            listVideoPick.clear()
        }
    }

    private fun initView() {
        val colors = intArrayOf(
            ContextCompat.getColor(this@SelectVideoActivity, R.color.color_1),
            ContextCompat.getColor(this@SelectVideoActivity, R.color.color_2)
        )
        binding.tvContinue.applyGradient(this@SelectVideoActivity,colors)
        binding.tvSelected.text = "$countVideo Selected"
    }

    private fun initData() {
        adapter = SelectVideoAdapter(this@SelectVideoActivity)
        adapter.getData(listVideo)
        binding.recVideo.adapter = adapter
    }

//    fun formatTimeToHoursMinutes(duration: Long): String {
//        val minutes = (duration / 1000) / 60
//        val seconds = (duration / 1000) % 60
//        return String.format("%02d:%02d", minutes, seconds)
//    }

    override fun onResume() {
        super.onResume()
        Log.d("check_crash", "onResume: "+ listVideoPick +  "_____" + listVideo)
        if(listVideoPick.size < listVideo.size){
            binding.imvTick.visibility = View.VISIBLE
            binding.imvTickTrue.visibility = View.GONE
        }
        binding.tvSelected.text = "$countVideo Selected"
        binding.tvSize.text = "/ $countSizeVideo MB"
        adapter.notifyDataSetChanged()
    }
}