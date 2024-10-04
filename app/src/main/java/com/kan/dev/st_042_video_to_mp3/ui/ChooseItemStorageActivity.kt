package com.kan.dev.st_042_video_to_mp3.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivityChooseItemStorageBinding
import com.kan.dev.st_042_video_to_mp3.model.AudioInfo
import com.kan.dev.st_042_video_to_mp3.ui.file_convert_to_mp3.FileConvertAdapter
import com.kan.dev.st_042_video_to_mp3.ui.fragment.storage.AudioAdapterFr
import com.kan.dev.st_042_video_to_mp3.utils.Const.countAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSize
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionAudioPlay
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.AbsBaseActivity

class ChooseItemStorageActivity: AbsBaseActivity<ActivityChooseItemStorageBinding>(false) {
    override fun getFragmentID(): Int  = 0

    override fun getLayoutId(): Int = R.layout.activity_choose_item_storage
    lateinit var adapterFr: AudioAdapterFr
    override fun init() {
        initData()
        initView()
        initAction()
    }

    private fun initAction() {
        binding.lnShare.onSingleClick {
            shareAudioUris(this@ChooseItemStorageActivity, listAudioPick)
        }
    }

    private fun initView() {
        binding.tvSelectedItem.text = "$countAudio Selected"
        binding.size.text = "$countSize MB"
    }

    private fun initData() {
        adapterFr = AudioAdapterFr(this)
        adapterFr.getData(listAudio)
        binding.recyclerView.adapter = adapterFr

        adapterFr.onClickListener(object : AudioAdapterFr.onClickItemListener{
            override fun onClickItem(position: Int, holder: AudioAdapterFr.ViewHolder) {
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
                binding.tvSelectedItem.text = "$countAudio Selected"
                binding.size.text = "$countSize KB"
            }

            override fun onTouchEven(position: Int) {
                Log.d("checjcjcjc", "onTouchEven: ")
            }
        })
    }

    fun shareAudioUris(context: Context, listAudio: MutableList<AudioInfo>) {
        // Tạo một danh sách các Uri
        val uriList: ArrayList<Uri> = ArrayList()
        for (audio in listAudio) {
            uriList.add(audio.uri) // Thêm Uri từ đối tượng AudioInfo
        }

        // Tạo Intent để chia sẻ danh sách Uri
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE // Chia sẻ nhiều tệp
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList) // Gửi danh sách Uri
            type = "audio/*" // Định dạng loại tệp
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Cấp quyền đọc cho các ứng dụng nhận
        }

        // Bắt đầu Intent chia sẻ
        context.startActivity(Intent.createChooser(shareIntent, "Share Audio Files"))
    }

}