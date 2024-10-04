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
import com.kan.dev.st_042_video_to_mp3.databinding.ActivitySelectAudioBinding
import com.kan.dev.st_042_video_to_mp3.model.AudioInfo
import com.kan.dev.st_042_video_to_mp3.ui.AudioSpeedActivity
import com.kan.dev.st_042_video_to_mp3.ui.audio_converter.AudioConverterActivity
import com.kan.dev.st_042_video_to_mp3.ui.audio_converter.AudioConverterAdapter
import com.kan.dev.st_042_video_to_mp3.ui.file_convert_to_mp3.FileConvertToMp3Activity
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.Const.checkDataAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.countAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSize
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionAudioPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionVideoPlay
import com.kan.dev.st_042_video_to_mp3.utils.applyGradient
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.AbsBaseActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SelectAudioActivity : AbsBaseActivity<ActivitySelectAudioBinding>(false) {
    override fun getFragmentID(): Int = 0

    override fun getLayoutId(): Int = R.layout.activity_select_audio

    var count = 0
    lateinit var adapter: SelectAudioAdapter
    override fun init() {
        initData()
        initView()
        if(Const.selectTypeAudio.equals("AudioSpeed")){
            initActionAudioSpeed()
        }else{
            initAction()
        }
    }

    private fun initActionAudioSpeed() {

        adapter.onClickListener(object : SelectAudioAdapter.onClickItemListener{
            override fun onClickItem(position: Int, holder: SelectAudioAdapter.ViewHolder) {
                if(!listAudio[position].active){
                    if(listAudioPick.size>0){
                        positionAudioPlay = position
                        val pos = listAudioPick[0].pos
                        listAudio[pos].active = false
                        listAudio[position].active = true
                        listAudioPick.removeAt(0)
                        listAudioPick.add(0,listAudio[position])
                        adapter.notifyDataSetChanged()
                    }else{
                        positionAudioPlay = position
                        countAudio += 1
                        countSize += listAudio[position].sizeInMB.toInt()
                        holder.binding.imvTick.setImageResource(R.drawable.icon_check_box_yes)
                        listAudioPick.add(0, listAudio[position])
                        listAudio[position].active = true
                    }
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
                startActivity(Intent(this@SelectAudioActivity, AudioSpeedActivity::class.java))
            }else{
                Toast.makeText(this@SelectAudioActivity, getString(R.string.you_must_choose_1_video), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initViewAudioSpeed() {

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
            getAllAudios(contentResolver)
            Log.d("check_list_video", "initData: "+ listAudio)
            checkDataAudio = true
        }
        adapter = SelectAudioAdapter(this@SelectAudioActivity)
        adapter.getData(listAudio)
        binding.recFileConvert.adapter = adapter
    }

    private fun initView() {
        val colors = intArrayOf(
            ContextCompat.getColor(this@SelectAudioActivity, R.color.color_1),
            ContextCompat.getColor(this@SelectAudioActivity, R.color.color_2)
        )
        binding.tvSelected.text = "$countAudio Selected"
        binding.tvContinue.applyGradient(this@SelectAudioActivity,colors)
    }

    fun getAllAudios(contentResolver: ContentResolver) {
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DURATION,      // Lấy thời gian của audio
            MediaStore.Audio.Media.SIZE,          // Lấy dung lượng audio
            MediaStore.Audio.Media.DISPLAY_NAME,  // Lấy tên audio
            MediaStore.Audio.Media.DATE_ADDED,    // Lấy ngày thêm (tính bằng giây từ Unix epoch)
            MediaStore.Audio.Media.MIME_TYPE      // Lấy định dạng audio
        )

        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val audioUri = Uri.withAppendedPath(uri, id.toString())
                val duration = cursor.getLong(durationColumn) // Thời gian của audio (milliseconds)
                val size = cursor.getLong(sizeColumn) // Lấy giá trị dung lượng (bytes)
                val sizeInMB = size / (1024 * 1024)  // Chuyển đổi sang MB
                val audioName = cursor.getString(nameColumn) // Lấy tên audio
                val dateAdded = cursor.getLong(dateAddedColumn) * 1000L
                val formattedDate = formatDate(dateAdded)

                listAudio.add(AudioInfo(audioUri, formatTimeToHoursMinutes(duration), sizeInMB, audioName, formattedDate, false, audioName,count))
                count+=1
            }
        }
    }

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