package com.kan.dev.st_042_video_to_mp3.ui.select_audio

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivityAudioMergerBinding
import com.kan.dev.st_042_video_to_mp3.databinding.ActivitySelectAudioBinding
import com.kan.dev.st_042_video_to_mp3.model.AudioInfo
import com.kan.dev.st_042_video_to_mp3.model.ElementCount
import com.kan.dev.st_042_video_to_mp3.ui.ActivityAudioCutter
import com.kan.dev.st_042_video_to_mp3.ui.AudioSpeedActivity
import com.kan.dev.st_042_video_to_mp3.ui.audio_converter.AudioConverterActivity
import com.kan.dev.st_042_video_to_mp3.ui.merger_audio.MergerAudioActivity
import com.kan.dev.st_042_video_to_mp3.utils.AudioUtils
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.Const.checkDataAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.clickItem
import com.kan.dev.st_042_video_to_mp3.utils.Const.countAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.countMap
import com.kan.dev.st_042_video_to_mp3.utils.Const.countPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSize
import com.kan.dev.st_042_video_to_mp3.utils.Const.elementCounts
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionAudioPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectTypeAudio
import com.kan.dev.st_042_video_to_mp3.utils.applyGradient
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.AbsBaseActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class SelectAudioActivity : AbsBaseActivity<ActivitySelectAudioBinding>(false) {
    override fun getFragmentID(): Int = 0
    override fun getLayoutId(): Int = R.layout.activity_select_audio
    val adapter by lazy {
        SelectAudioAdapter(this@SelectAudioActivity)
    }
    var countItemt = 1
    private var mediaPlayer: MediaPlayer? = null
    override fun init() {
        initData()
        initView()
        initActionBack()
    }

    private fun initActionBack() {
        binding.imvBack.onSingleClick {
            finish()
            if(mediaPlayer?.isPlaying == true){
                mediaPlayer!!.release()
            }
        }
    }

    private fun initActionAudioMerger() {
        binding.tvContinue.onSingleClick {
            Log.d("check_list_audio_pick", "initActionAudioMerger: "+ listAudioPick)
            if (listAudioPick.size < 2){
                Toast.makeText(this@SelectAudioActivity, getString(R.string.you_must_choose_2_or_more_items), Toast.LENGTH_SHORT).show()
            }else{
                startActivity(Intent(this@SelectAudioActivity,MergerAudioActivity::class.java))
            }
        }
        adapter.onClickListener(object : SelectAudioAdapter.onClickItemListener{
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onClickItem(position: Int, holder: SelectAudioAdapter.ViewHolder) {
                if(!listAudio[position].active){
                    positionAudioPlay = position
                    countAudio += 1
                    countSize += listAudio[position].sizeInMB.toInt()
                    clickItem = true
                    holder.binding.edtStartTime.setText("1")
                    holder.binding.imvTick.setImageResource(R.drawable.icon_check_box_yes)
                    listAudio[position].active = true
                    listAudioPick.add(listAudio[position])
                    dataItemChange()
                    adapter.notifyDataSetChanged()
                }else if(listAudio[position].active){
                    var countEdt = Integer.parseInt(holder.binding.edtStartTime.text.toString())
                    countAudio -= countEdt
                    holder.binding.imvTick.setImageResource(R.drawable.icon_check_box)
                    countItemt = 1
                    listAudioPick.removeAll { it.name == listAudio[position].name }
                    dataItemChange()
                    Log.d("check_list_audio_pick____", "onClickItem: "  + listAudioPick.size   + " ____ " + listAudio[position])
                    listAudio[position].active = false
                    countSize -= countEdt*(listAudio[position].sizeInMB.toInt())
                    adapter.notifyDataSetChanged()
                }
                binding.tvSelected.text = "$countAudio ${getString(R.string.selected)}"
                binding.tvSize.text = "/ $countSize MB"
            }

            override fun onClickPlayAudio(position: Int, holder: SelectAudioAdapter.ViewHolder) {
                val previouslySelectedIndex = listAudio.indexOfFirst { it.activePl }
                if (previouslySelectedIndex != -1 && previouslySelectedIndex != position) {
                    listAudio[previouslySelectedIndex].activePl = false
                    adapter.notifyItemChanged(previouslySelectedIndex)
                    // Release media cho item trước đó
                    mediaPlayer?.release()
                    mediaPlayer = null
                }
                // Nếu item hiện tại chưa phát, bắt đầu phát
                if (!listAudio[position].activePl) {
                    holder.binding.imvPlayVideo.setImageResource(R.drawable.imv_pause_audio)
                    listAudio[position].activePl = true
                    if(mediaPlayer == null){
                        mediaPlayer = setupMediaPlayer(this@SelectAudioActivity, listAudio[position].uri)
                    }
                    if(mediaPlayer!= null){
                        mediaPlayer!!.start()
                    }

                } else {
                    // Nếu item hiện tại đang phát, tạm dừng
                    holder.binding.imvPlayVideo.setImageResource(R.drawable.imv_play_audio)
                    listAudio[position].activePl = false
                    mediaPlayer?.pause()
                }
                adapter.notifyItemChanged(position)
            }
        })

        adapter.onClickEdtListener(object :SelectAudioAdapter.onClickItemListenerEdt{
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onPlusItem(position: Int, holder: SelectAudioAdapter.ViewHolder) {
                countItemt = Integer.parseInt(holder.binding.edtStartTime.text.toString())
                countItemt += 1
                positionAudioPlay = position
                countAudio += 1
                countSize += listAudio[position].sizeInMB.toInt()
                listAudioPick.add(listAudio[position])
                dataItemChange()
                holder.binding.edtStartTime.setText(countItemt.toString())
                binding.tvSelected.text = "$countAudio Selected"
                binding.tvSize.text = "/ $countSize MB"
                adapter.notifyDataSetChanged()
            }
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onMinusItem(position: Int, holder: SelectAudioAdapter.ViewHolder) {
                countItemt = Integer.parseInt(holder.binding.edtStartTime.text.toString())
                countItemt -= 1
                listAudioPick.remove(listAudio[position])
                countSize -= listAudio[position].sizeInMB.toInt()
                dataItemChange()
                countAudio -= 1
                if(countItemt == 0){
                    holder.binding.imvTick.setImageResource(R.drawable.icon_check_box)
                    holder.binding.lnItemCount.visibility = View.GONE
                    listAudio[position].active = false
                    holder.binding.tvTime.visibility = View.GONE
                    countItemt = 1
                }
                binding.tvSelected.text = "$countAudio Selected"
                binding.tvSize.text = "/ $countSize MB"
                holder.binding.edtStartTime.setText(countItemt.toString())
            }

        })


    }

    private fun initActionAudioSpeed() {
        adapter.onClickListener(object : SelectAudioAdapter.onClickItemListener{
            override fun onClickItem(position: Int, holder: SelectAudioAdapter.ViewHolder) {
                val previouslySelectedIndex = listAudio.indexOfFirst { it.active }
                if (previouslySelectedIndex != -1 && previouslySelectedIndex != position) {
                    positionAudioPlay = position
                    listAudio[previouslySelectedIndex].active = false
                    countAudio -= 1
                    countSize -= listAudio[previouslySelectedIndex].sizeInMB.toInt()
                    adapter.notifyItemChanged(previouslySelectedIndex) // Cập nhật lại item trước đó
                }
                if (!listAudio[position].active) {
                    positionAudioPlay = position
                    countAudio += 1
                    countSize += listAudio[position].sizeInMB.toInt()
                    holder.binding.imvTick.setImageResource(R.drawable.icon_check_box_yes)
                    listAudioPick.clear()
                    listAudio[position].active = true// Xóa danh sách đã chọn trước đó
                    listAudioPick.add(listAudio[position])
                } else {
                    countAudio -= 1
                    holder.binding.imvTick.setImageResource(R.drawable.icon_check_box)
                    listAudioPick.remove(listAudio[position])
                    listAudio[position].active = false
                    countSize -= listAudio[position].sizeInMB.toInt()
                }
                binding.tvSelected.text = "$countAudio Selected"
                binding.tvSize.text = "/ $countSize MB"
            }

            override fun onClickPlayAudio(position: Int, holder: SelectAudioAdapter.ViewHolder) {
                val previouslySelectedIndex = listAudio.indexOfFirst { it.activePl }
                if (previouslySelectedIndex != -1 && previouslySelectedIndex != position) {
                    listAudio[previouslySelectedIndex].activePl = false
                    adapter.notifyItemChanged(previouslySelectedIndex)
                    // Release media cho item trước đó
                    mediaPlayer?.release()
                    mediaPlayer = null
                }
                // Nếu item hiện tại chưa phát, bắt đầu phát
                if (!listAudio[position].activePl) {
                    holder.binding.imvPlayVideo.setImageResource(R.drawable.imv_pause_audio)
                    listAudio[position].activePl = true
                    if(mediaPlayer == null){
                        mediaPlayer = setupMediaPlayer(this@SelectAudioActivity, listAudio[position].uri)
                    }
                    mediaPlayer?.start()
                } else {
                    // Nếu item hiện tại đang phát, tạm dừng
                    holder.binding.imvPlayVideo.setImageResource(R.drawable.imv_play_audio)
                    listAudio[position].activePl = false
                    mediaPlayer?.pause()
                }
                adapter.notifyItemChanged(position)
            }
        })


        binding.lnContinue.onSingleClick {
//            startActivity(Intent(this@SelectAudioActivity, ActivityAudioCutter::class.java))
            if(listAudioPick.size > 0 && selectTypeAudio.equals("AudioSpeed")){
                startActivity(Intent(this@SelectAudioActivity, AudioSpeedActivity::class.java))
            }else if(listAudioPick.size>0 && selectTypeAudio.equals("AudioCutter")){
                startActivity(Intent(this@SelectAudioActivity, ActivityAudioCutter::class.java))
            } else{
                Toast.makeText(this@SelectAudioActivity, getString(R.string.you_must_choose_1_video), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initAction() {
        adapter.onClickListener(object : SelectAudioAdapter.onClickItemListener{
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onClickItem(position: Int, holder: SelectAudioAdapter.ViewHolder) {
                if(!listAudio[position].active){
                    positionAudioPlay = position
                    countAudio += 1
                    countSize += listAudio[position].sizeInMB.toInt()
                    holder.binding.imvTick.setImageResource(R.drawable.icon_check_box_yes)
                    listAudio[position].active = true
                    listAudioPick.add(0, listAudio[position])
                }else if(listAudio[position].active){
                    countAudio -= 1
                    holder.binding.imvTick.setImageResource(R.drawable.icon_check_box)
                    listAudioPick.remove(listAudio[position])
                    listAudio[position].active = false
                    countSize -= listAudio[position].sizeInMB.toInt()
                }
                binding.tvSelected.text = "$countAudio Selected"
                binding.tvSize.text = "/ $countSize MB"
            }

            override fun onClickPlayAudio(position: Int, holder: SelectAudioAdapter.ViewHolder) {
                val previouslySelectedIndex = listAudio.indexOfFirst { it.activePl }
                if (previouslySelectedIndex != -1 && previouslySelectedIndex != position) {
                    listAudio[previouslySelectedIndex].activePl = false
                    adapter.notifyItemChanged(previouslySelectedIndex)
                    mediaPlayer?.release()
                    mediaPlayer = null
                }
                if (!listAudio[position].activePl) {
                    holder.binding.imvPlayVideo.setImageResource(R.drawable.imv_pause_audio)
                    listAudio[position].activePl = true
                    if(mediaPlayer == null){
                        mediaPlayer = setupMediaPlayer(this@SelectAudioActivity, listAudio[position].uri)
                    }
                    mediaPlayer?.start()
                } else {
                    holder.binding.imvPlayVideo.setImageResource(R.drawable.imv_play_audio)
                    listAudio[position].activePl = false
                    mediaPlayer?.pause()
                }
                adapter.notifyItemChanged(position)
            }
        })
        binding.lnContinue.onSingleClick {
            if(listAudioPick.size>0){
                startActivity(Intent(this@SelectAudioActivity, AudioConverterActivity::class.java))
            }else{
                Toast.makeText(this@SelectAudioActivity, getString(R.string.you_must_choose_1_video), Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun initData() {
        if (!checkDataAudio){
            AudioUtils.getAllAudios(contentResolver)
            checkDataAudio = true
        }
        Log.d("check_data_list", "initData: "+ listAudio.size + ":   "+ listAudio)
//        adapter = SelectAudioAdapter(this@SelectAudioActivity)
        adapter.getData(listAudio)
        binding.recFileConvert.adapter = adapter
//        binding.recFileConvert.itemAnimator = null
        if(selectTypeAudio.equals("AudioSpeed") || Const.selectTypeAudio.equals("AudioCutter")){
            Log.d("check_click", "init: okeeeeeeeeeeeeee")
            initActionAudioSpeed()
        }else if(selectTypeAudio.equals("AudioMerger")){
            initActionAudioMerger()
        } else{
            initAction()
        }
    }

    private fun initView() {
        val colors = intArrayOf(
            ContextCompat.getColor(this@SelectAudioActivity, R.color.color_1),
            ContextCompat.getColor(this@SelectAudioActivity, R.color.color_2)
        )
        binding.tvSelected.text = "$countAudio Selected"
        binding.tvContinue.applyGradient(this@SelectAudioActivity,colors)

        if(listAudio.size == 0){
            binding.noItem.visibility = View.VISIBLE
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

    @RequiresApi(Build.VERSION_CODES.N)
    fun dataItemChange (){
        countMap.clear()
        for (audio in listAudioPick) {
            countMap[audio.name] = countMap.getOrDefault(audio.name, 0) + 1
        }
        elementCounts = countMap.map { (name, count) ->
            ElementCount(name, count)
        }.toMutableList()
        adapter.notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onResume() {
        super.onResume()
        countPlay = 0
        if(selectTypeAudio.equals("AudioMerger")){
            dataItemChange()
        }
        listAudio.forEach { it.activePl = false }
        adapter.notifyDataSetChanged()
        binding.tvSelected.text = "$countAudio Selected"
    }
    override fun onStop() {
        super.onStop()
        if (mediaPlayer!=null){
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
}