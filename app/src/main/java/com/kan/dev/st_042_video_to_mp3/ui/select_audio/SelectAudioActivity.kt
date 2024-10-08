package com.kan.dev.st_042_video_to_mp3.ui.select_audio

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
import com.kan.dev.st_042_video_to_mp3.databinding.ActivityAudioMergerBinding
import com.kan.dev.st_042_video_to_mp3.databinding.ActivitySelectAudioBinding
import com.kan.dev.st_042_video_to_mp3.model.AudioInfo
import com.kan.dev.st_042_video_to_mp3.ui.ActivityAudioCutter
import com.kan.dev.st_042_video_to_mp3.ui.AudioSpeedActivity
import com.kan.dev.st_042_video_to_mp3.ui.audio_converter.AudioConverterActivity
import com.kan.dev.st_042_video_to_mp3.ui.audio_converter.AudioConverterAdapter
import com.kan.dev.st_042_video_to_mp3.ui.file_convert_to_mp3.FileConvertToMp3Activity
import com.kan.dev.st_042_video_to_mp3.ui.merger_audio.MergerAudioActivity
import com.kan.dev.st_042_video_to_mp3.utils.AudioUtils
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.Const.checkDataAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.countAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSize
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSizeVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.countVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionAudioPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionVideoPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectType
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
    lateinit var adapter: SelectAudioAdapter
    var countItemt = 1
    override fun init() {
        initData()
        initView()
        if(Const.selectTypeAudio.equals("AudioSpeed") || Const.selectTypeAudio.equals("AudioCutter")){
            Log.d("check_click", "init: okeeeeeeeeeeeeee")
            initActionAudioSpeed()
        }else if(selectTypeAudio.equals("AudioMerger")){
            initActionAudioMerger()
        }
        else{
            initAction()
        }
    }
    private fun initActionAudioMerger() {
        binding.tvContinue.onSingleClick {
            Log.d("check_list_audio_pick", "initActionAudioMerger: "+ listAudioPick)
            startActivity(Intent(this@SelectAudioActivity,MergerAudioActivity::class.java))
        }
        adapter.onClickListener(object : SelectAudioAdapter.onClickItemListener{
            override fun onClickItem(position: Int, holder: SelectAudioAdapter.ViewHolder) {
                if(!listAudio[position].active){
                    positionAudioPlay = position
                    countAudio += 1
                    holder.binding.lnItemCount.visibility = View.VISIBLE
                    countSize += listAudio[position].sizeInMB.toInt()
                    holder.binding.edtStartTime.setText("1")
                    holder.binding.tvTime.visibility = View.INVISIBLE
                    holder.binding.imvTick.setImageResource(R.drawable.icon_check_box_yes)
                    listAudioPick.add(0, listAudio[position])
                    listAudio[position].active = true
                }else if(listAudio[position].active){
                    var countEdt = Integer.parseInt(holder.binding.edtStartTime.text.toString())
                    countAudio -= countEdt
                    holder.binding.imvTick.setImageResource(R.drawable.icon_check_box)
                    holder.binding.lnItemCount.visibility = View.GONE
                    listAudio[position].active = false
                    holder.binding.tvTime.visibility = View.GONE
                    countItemt = 1
                    Log.d("check_list_audio_pick", "onClickItem: "  + listAudio.size   + " ____ " + position)
                    listAudioPick.removeAll { it.pos == listAudio.size - position -1 }
                    countSize -= countEdt*(listAudio[position].sizeInMB.toInt())
                }
                binding.tvSelected.text = "$countAudio Selected"
                binding.tvSize.text = "/ $countSize MB"
            }
        })

        adapter.onClickEdtListener(object :SelectAudioAdapter.onClickItemListenerEdt{
            override fun onPlusItem(position: Int, holder: SelectAudioAdapter.ViewHolder) {
                countItemt = Integer.parseInt(holder.binding.edtStartTime.text.toString())
                countItemt += 1
                positionAudioPlay = position
                countAudio += 1
                countSize += listAudio[position].sizeInMB.toInt()
                listAudioPick.add(0, listAudio[position])
                holder.binding.edtStartTime.setText(countItemt.toString())
                binding.tvSelected.text = "$countAudio Selected"
                binding.tvSize.text = "/ $countSize MB"
            }
            override fun onMinusItem(position: Int, holder: SelectAudioAdapter.ViewHolder) {
                countItemt = Integer.parseInt(holder.binding.edtStartTime.text.toString())
                countItemt -= 1
                listAudioPick.remove(listAudio[position])
                countSize -= listAudio[position].sizeInMB.toInt()
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

        binding.imvBack.onSingleClick {
            finish()
        }
    }

    private fun initActionAudioSpeed() {
        adapter.onClickListener(object : SelectAudioAdapter.onClickItemListener{
            override fun onClickItem(position: Int, holder: SelectAudioAdapter.ViewHolder) {
                val previouslySelectedIndex = listAudio.indexOfFirst { it.active }
                if (previouslySelectedIndex != -1 && previouslySelectedIndex != position) {
                    // Nếu có item được chọn trước đó và không phải item đang click
                    listAudio[previouslySelectedIndex].active = false
                    countAudio -= 1
                    countSize -= listAudio[previouslySelectedIndex].sizeInMB.toInt()
                    adapter.notifyItemChanged(previouslySelectedIndex) // Cập nhật lại item trước đó
                }

                // Chọn item mới
                if (!listAudio[position].active) {
                    positionAudioPlay = position
                    countAudio += 1
                    countSize += listAudio[position].sizeInMB.toInt()
                    holder.binding.imvTick.setImageResource(R.drawable.icon_check_box_yes)
                    listAudio[position].active = true
                    listAudioPick.clear() // Xóa danh sách đã chọn trước đó
                    listAudioPick.add(0, listAudio[position])
                } else {
                    countAudio -= 1
                    holder.binding.imvTick.setImageResource(R.drawable.icon_check_box)
                    listAudio[position].active = false
                    listAudioPick.remove(listAudio[position])
                    countSize -= listAudio[position].sizeInMB.toInt()
                }

                binding.tvSelected.text = "$countAudio Selected"
                binding.tvSize.text = "/ $countSize MB"
            }
        })

        binding.imvBack.onSingleClick {
            finish()
        }

        binding.lnContinue.onSingleClick {
            if(countAudio > 0 && selectTypeAudio.equals("AudioSpeed")){
                startActivity(Intent(this@SelectAudioActivity, AudioSpeedActivity::class.java))
            }else if(countAudio>0 && selectTypeAudio.equals("AudioCutter")){
                startActivity(Intent(this@SelectAudioActivity, ActivityAudioCutter::class.java))
            } else{
                Toast.makeText(this@SelectAudioActivity, getString(R.string.you_must_choose_1_video), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initAction() {
        adapter.onClickListener(object : SelectAudioAdapter.onClickItemListener{
            override fun onClickItem(position: Int, holder: SelectAudioAdapter.ViewHolder) {
                if(!listAudio[position].active){
                    positionAudioPlay = position
                    countAudio += 1
                    countSize += listAudio[position].sizeInMB.toInt()
                    holder.binding.imvTick.setImageResource(R.drawable.icon_check_box_yes)
                    listAudioPick.add(0, listAudio[position])
                    listAudio[position].active = true
                }else if(listAudio[position].active){
                    countAudio -= 1
                    holder.binding.imvTick.setImageResource(R.drawable.icon_check_box)
                    listAudio[position].active = false
                    listAudioPick.remove(listAudio[position])
                    countSize -= listAudio[position].sizeInMB.toInt()
                }
                binding.tvSelected.text = "$countAudio Selected"
                binding.tvSize.text = "/ $countSize MB"
            }
        })

        binding.imvBack.onSingleClick {
            finish()
            listAudioPick.clear()
            listAudio.clear()
        }

        binding.lnContinue.onSingleClick {
            if(countAudio>0){
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
        adapter = SelectAudioAdapter(this@SelectAudioActivity)
        adapter.getData(listAudio)
        binding.recFileConvert.adapter = adapter
        binding.recFileConvert.itemAnimator = null
    }

    private fun initView() {
        val colors = intArrayOf(
            ContextCompat.getColor(this@SelectAudioActivity, R.color.color_1),
            ContextCompat.getColor(this@SelectAudioActivity, R.color.color_2)
        )
        binding.tvSelected.text = "$countAudio Selected"
        binding.tvContinue.applyGradient(this@SelectAudioActivity,colors)
    }

//    fun getAllAudios(contentResolver: ContentResolver) {
//        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
//        val projection = arrayOf(
//            MediaStore.Audio.Media._ID,
//            MediaStore.Audio.Media.DURATION,      // Lấy thời gian của audio
//            MediaStore.Audio.Media.SIZE,          // Lấy dung lượng audio
//            MediaStore.Audio.Media.DISPLAY_NAME,  // Lấy tên audio
//            MediaStore.Audio.Media.DATE_ADDED,    // Lấy ngày thêm (tính bằng giây từ Unix epoch)
//            MediaStore.Audio.Media.MIME_TYPE      // Lấy định dạng audio
//        )
//
//        val cursor: Cursor? = contentResolver.query(
//            uri,
//            projection,
//            null,
//            null,
//            null
//        )
//
//        cursor?.use {
//            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
//            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
//            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
//            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
//            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
//
//            while (cursor.moveToNext()) {
//                val id = cursor.getLong(idColumn)
//                val audioUri = Uri.withAppendedPath(uri, id.toString())
//                val duration = cursor.getLong(durationColumn) // Thời gian của audio (milliseconds)
//                val size = cursor.getLong(sizeColumn) // Lấy giá trị dung lượng (bytes)
//                val sizeInMB = size / (1024 * 1024)  // Chuyển đổi sang MB
//                val audioName = cursor.getString(nameColumn) // Lấy tên audio
//                val dateAdded = cursor.getLong(dateAddedColumn) * 1000L
//                val formattedDate = formatDate(dateAdded)
//
//                listAudio.add(AudioInfo(audioUri, formatTimeToHoursMinutes(duration), sizeInMB, audioName, formattedDate, false, audioName,count))
//                count+=1
//            }
//        }
//    }

    override fun onResume() {
        super.onResume()
        binding.tvSelected.text = "$countAudio Selected"
        adapter.notifyDataSetChanged()
    }

    fun formatTimeToHoursMinutes(duration: Long): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) // Định dạng dd MMM yyyy
        val date = Date(timestamp)
        return sdf.format(date)
    }

}