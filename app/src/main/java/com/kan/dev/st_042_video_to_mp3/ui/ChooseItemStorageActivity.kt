package com.kan.dev.st_042_video_to_mp3.ui

import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.MediaStore.Audio
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivityChooseItemStorageBinding
import com.kan.dev.st_042_video_to_mp3.databinding.CustomDialogRingtoneBinding
import com.kan.dev.st_042_video_to_mp3.databinding.CustomeDialogDeleteBinding
import com.kan.dev.st_042_video_to_mp3.model.AudioInfo
import com.kan.dev.st_042_video_to_mp3.ui.file_convert_to_mp3.FileConvertAdapter
import com.kan.dev.st_042_video_to_mp3.ui.fragment.storage.AudioAdapterFr
import com.kan.dev.st_042_video_to_mp3.utils.Const.countAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSize
import com.kan.dev.st_042_video_to_mp3.utils.Const.currentRingtone
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionAudioPlay
import com.kan.dev.st_042_video_to_mp3.utils.SystemUtils
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.AbsBaseActivity

class ChooseItemStorageActivity: AbsBaseActivity<ActivityChooseItemStorageBinding>(false) {
    override fun getFragmentID(): Int  = 0

    override fun getLayoutId(): Int = R.layout.activity_choose_item_storage
    lateinit var adapterFr: AudioAdapterFr
    override fun init() {
        adapterFr = AudioAdapterFr(this)
        initData()
        initView()
        initAction()
    }

    private fun initAction() {
        binding.lnShare.onSingleClick {
            shareAudioUris(this@ChooseItemStorageActivity, listAudioPick)
        }

        binding.imvDelete.onSingleClick {
            showDialogDelete()
        }

        binding.imvRingtone.onSingleClick {
            showDialogRingtone()
        }
        binding.lnCancel.onSingleClick {
            finish()
        }
    }

    private fun showDialogDelete() {
        SystemUtils.setLocale(this)
        val dialogBinding  = CustomeDialogDeleteBinding.inflate(LayoutInflater.from(this))
        val dialog = Dialog(this)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.window?.setBackgroundDrawable(getDrawable(R.color.transparent))
        dialog.setContentView(dialogBinding.root)
        var position = 0
        dialog.setCancelable(false)

        dialogBinding.tvYes.onSingleClick{
            for (i in 0..listAudioPick.size-1){
                var pos = listAudioPick[i].pos
                if(listAudio[pos].active){
                    listAudio.removeAt(i)
                    adapterFr.notifyItemRemoved(i)
                    adapterFr.notifyDataSetChanged()
                }
            }
            deleteFilesByUri(contentResolver, listAudioPick)
//
            dialog.dismiss()
        }
        dialogBinding.tvNo.onSingleClick {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showDialogRingtone() {
        SystemUtils.setLocale(this)
        val dialogBinding  = CustomDialogRingtoneBinding.inflate(LayoutInflater.from(this))
        val dialog = Dialog(this)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.window?.setBackgroundDrawable(getDrawable(R.color.transparent))
        dialog.setContentView(dialogBinding.root)
        var position = 0
        dialog.setCancelable(false)
        dialogBinding.rdGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
            for (i in 0 until radioGroup.childCount) {
                val radioButton = radioGroup.getChildAt(i) as? RadioButton
                if (radioButton?.id == checkedId) {
                    position = i
                    break
                }
            }
        }
        dialogBinding.lnSave.onSingleClick{
            when(position){
                0 -> handleWriteSettingsPermission(0)
                1 -> handleWriteSettingsPermission(1)
                2 -> handleWriteSettingsPermission(2)
            }
            currentRingtone = position
            dialog.dismiss()
        }
        dialogBinding.lnCancel.onSingleClick {
            dialog.dismiss()
        }
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun handleWriteSettingsPermission(type: Int) {
        val permission: Boolean = Settings.System.canWrite(this)
        if (permission) {
            setRingtone(type)
        } else {
            openAndroidPermissionsMenu()
        }
    }

    private fun openAndroidPermissionsMenu() {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.setData(Uri.parse("package:" + this.packageName))
        startActivity(intent)
    }

    private fun setRingtone(type: Int) {
        val customRingtoneUri = Uri.parse(listAudioPick[0].uri.toString())
        when (type) {
            0       -> {
                RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, customRingtoneUri)
            }

            1 -> {
                RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION, customRingtoneUri)
            }

            2-> {
                RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM, customRingtoneUri)
            }

            else                                                   -> {}
        }
        Toast.makeText(this@ChooseItemStorageActivity, R.string.set_ringtone_successfully, Toast.LENGTH_SHORT).show()
    }

    fun deleteFilesByUri(contentResolver: ContentResolver, listAudioP: List<AudioInfo>) {
        val uriList: ArrayList<Uri> = ArrayList()
        for (audio in listAudioP) {
            uriList.add(Uri.parse(getRealPathFromURI(this@ChooseItemStorageActivity,audio.uri)))
        }
        for (uri in uriList) {
            try {
                val rowsDeleted = contentResolver.delete(uri, null, null)
                if (rowsDeleted > 0) {
                    println("Deleted: $uri")
                } else {
                    println("Failed to delete: $uri")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Error deleting: $uri")
            }
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

    private fun initView() {
        binding.tvSelectedItem.text = "$countAudio Selected"
        binding.size.text = "$countSize MB"
    }

    private fun initData() {

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

                if(countAudio > 1){
                    binding.imvRename.visibility = View.GONE
                    binding.imvRingtone.visibility = View.GONE
                }else{
                    binding.imvRename.visibility = View.VISIBLE
                    binding.imvRingtone.visibility = View.VISIBLE
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