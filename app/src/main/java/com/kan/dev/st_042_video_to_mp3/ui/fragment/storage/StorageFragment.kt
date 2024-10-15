package com.kan.dev.st_042_video_to_mp3.ui.fragment.storage
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.media3.common.util.UnstableApi
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.CustomDialogRenameBinding
import com.kan.dev.st_042_video_to_mp3.databinding.CustomDialogRingtoneBinding
import com.kan.dev.st_042_video_to_mp3.databinding.CustomeDialogDeleteBinding
import com.kan.dev.st_042_video_to_mp3.databinding.FragmentStorageBinding
import com.kan.dev.st_042_video_to_mp3.interface_bottom.BottomNavVisibilityListener
import com.kan.dev.st_042_video_to_mp3.model.AudioInfo
import com.kan.dev.st_042_video_to_mp3.model.VideoInfo
import com.kan.dev.st_042_video_to_mp3.ui.ActivityAboutUs
import com.kan.dev.st_042_video_to_mp3.ui.PlaySongActivity
import com.kan.dev.st_042_video_to_mp3.ui.PlayVideoActivity
import com.kan.dev.st_042_video_to_mp3.utils.AudioUtils
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.Const.audioInfo
import com.kan.dev.st_042_video_to_mp3.utils.Const.audioInformation
import com.kan.dev.st_042_video_to_mp3.utils.Const.checkData
import com.kan.dev.st_042_video_to_mp3.utils.Const.checkDataAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.checkType
import com.kan.dev.st_042_video_to_mp3.utils.Const.countAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSize
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSizeVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.countVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.currentRingtone
import com.kan.dev.st_042_video_to_mp3.utils.Const.isTouchEventHandled
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioStorage
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoStorage
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionAudioPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionVideoPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.typefr
import com.kan.dev.st_042_video_to_mp3.utils.Const.uriPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.videoInfo
import com.kan.dev.st_042_video_to_mp3.utils.SystemUtils
import com.kan.dev.st_042_video_to_mp3.utils.VideoUtils
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.kan.dev.st_042_video_to_mp3.view_model.SharedViewModel
import com.metaldetector.golddetector.finder.SharedPreferenceUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class StorageFragment : Fragment() {
    lateinit var binding : FragmentStorageBinding
    val adapterFr by lazy {
        AudioAdapterFr(requireContext())
    }
    val adapterVdFr by lazy {
        VideoAdapterFr(requireContext())
    }
    var isClick = false
    private val viewModel: SharedViewModel by activityViewModels()
    private var listener: BottomNavVisibilityListener? = null
    lateinit var shareData : SharedPreferenceUtils
    override fun onStart() {
        super.onStart()
        checkType = true
    }
    var storageMusic = ""
    var storageVideo = ""
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavVisibilityListener) {
            listener = context
        } else {
            throw ClassCastException("$context must implement BottomNavVisibilityListener")
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStorageBinding.inflate(layoutInflater,container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initAction()
    }

    private fun initAction() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        typefr = "vd"
                        adapterVdFr.getData(listVideoStorage)
                        isClick = false
                        binding.recyclerViewTab1.adapter = adapterVdFr
                        binding.recyclerViewTab1.visibility = View.VISIBLE
                        binding.recyclerViewTab2.visibility = View.GONE
                        if(listVideoStorage.size == 0){
                            binding.lnNoItem.visibility = View.VISIBLE
                            binding.tvType.text = getString(R.string.you_don_t_have_any_content_yet_create_a_new_video_now)
                        }else{
                            binding.lnNoItem.visibility = View.GONE
                        }
                    }
                    1 -> {
                        typefr ="ad"
                        initRec()
                        isClick = false
                        binding.recyclerViewTab1.visibility = View.GONE
                        binding.recyclerViewTab2.visibility = View.VISIBLE
                        if(listAudioStorage.isEmpty()){
                            binding.lnNoItem.visibility = View.VISIBLE
                            binding.tvType.text = getString(R.string.you_don_t_have_any_content_yet_create_a_new_audio_now)
                        }else {
                            binding.lnNoItem.visibility = View.GONE
                        }
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        binding.imvAboutUs.onSingleClick {
            startActivity(Intent(requireContext(), ActivityAboutUs::class.java))
        }

        adapterVdFr.onClickListener(object : VideoAdapterFr.onClickItemListener{
            @OptIn(UnstableApi::class)
            override fun onClickItem(position: Int, holder: VideoAdapterFr.ViewHolder) {
                if (isTouchEventHandled  && isClick == true) {
                    if(!listVideoStorage[position].active){
                        positionVideoPlay = position
                        countVideo += 1
                        countSizeVideo += listVideoStorage[position].sizeInMB.toInt()
                        holder.binding.imvTickBox.setImageResource(R.drawable.icon_check_box_yes)
                        listVideoPick.add( listVideoStorage[position])
                        listVideoStorage[position].active = true
                    }else if(listVideoStorage[position].active){
                        countVideo -= 1
                        holder.binding.imvTickBox.setImageResource(R.drawable.icon_check_box)
                        listVideoStorage[position].active = false
                        listVideoPick.remove(listVideoStorage[position])
                        countSizeVideo -= listVideoStorage[position].sizeInMB.toInt()
                    }
                    if(countVideo <= 1){
                        binding.imvRename.visibility = View.VISIBLE
                    }else{
                        binding.imvRename.visibility = View.GONE
                    }
                    binding.tvSelectedItem.text = "$countVideo Selected"
                    binding.size.text = "$countSizeVideo KB"
                }else{
                    Log.d("check_logg", "onClickEven:  9liulk8iku8l8ul")
                    positionAudioPlay = position
                    videoInfo = listVideoStorage[position]
                    uriPlay = videoInfo!!.uri
                    startActivity(Intent(requireContext(), PlayVideoActivity::class.java))
                }
            }
            override fun onTouchEven(position: Int) {
                if(!isTouchEventHandled){
                    isTouchEventHandled = true
                    isClick = true
                    binding.imvRingtone.visibility = View.GONE
                    checkType = false
                    Const.positionVideoPlay = position
                    binding.ctlStorage.visibility = View.INVISIBLE
                    binding.ctlItem.visibility = View.VISIBLE
                    listener?.onBottomNavVisibilityChanged(false)
                    adapterVdFr.notifyDataSetChanged()
                }

            }
        })
        adapterFr.onClickListener(object : AudioAdapterFr.onClickItemListener{
            override fun onClickItem(position: Int, holder: AudioAdapterFr.ViewHolder) {
                if (isTouchEventHandled) {
                    if(!listAudioStorage[position].active){
                        positionAudioPlay = position
                        countAudio += 1
                        countSize += listAudioStorage[position].sizeInMB.toInt()
                        holder.binding.imvTick.setImageResource(R.drawable.icon_check_box_yes)
                        listAudioPick.add( listAudioStorage[position])
                        listAudioStorage[position].active = true
                    }else if(listAudioStorage[position].active){
                        countAudio -= 1
                        holder.binding.imvTick.setImageResource(R.drawable.icon_check_box)
                        listAudioStorage[position].active = false
                        listAudioPick.remove(listAudioStorage[position])
                        countSize -= listAudioStorage[position].sizeInMB.toInt()
                    }
                    if(countAudio <= 1){
                        binding.imvRename.visibility = View.VISIBLE
                        binding.imvRingtone.visibility = View.VISIBLE
                    }else{
                        binding.imvRename.visibility = View.GONE
                        binding.imvRingtone.visibility = View.GONE
                    }

                    binding.tvSelectedItem.text = "$countAudio Selected"
                    binding.size.text = "$countSize KB"
                }else{
                    Log.d("check_logg", "onClickEven:  9liulk8iku8l8ul")
                    Const.positionAudioPlay = position
                    isClick = true
                    uriPlay = listAudioStorage[position].uri
                    audioInformation = listAudioStorage[position]
                    Log.d("check_data", "onClickItem: "+ audioInformation)
                    startActivity(Intent(requireContext(), PlaySongActivity::class.java))
                }
            }
            override fun onTouchEven(position: Int) {
                if(!isTouchEventHandled){
                    Log.d("check_logg", "onTouchEven:  ojkeeeee")
                    checkType = false
                    isClick = true
                    isTouchEventHandled = true
                    Const.positionAudioPlay = position
                    binding.ctlStorage.visibility = View.INVISIBLE
                    binding.ctlItem.visibility = View.VISIBLE
                    listener?.onBottomNavVisibilityChanged(false)
                    adapterFr.notifyDataSetChanged()
                }
            }
        })

        viewModel.eventTriggered.observe(viewLifecycleOwner) { triggered ->
            if (triggered) {
                isTouchEventHandled = false
                checkType = true
                countAudio = 0
                countSize = 0
                countSizeVideo = 0
                countVideo = 0
                checkType = true
                adapterVdFr.notifyDataSetChanged()
                adapterFr.notifyDataSetChanged()
                listVideoStorage.clear()
                listAudioStorage.clear()
                VideoUtils.getAllVideosFromSpecificDirectory(storageVideo)
                AudioUtils.getAllAudiosFromSpecificDirectory_1(storageMusic)
                viewModel.resetEvent()
                binding.ctlStorage.visibility = View.VISIBLE
                binding.ctlItem.visibility = View.GONE
            }
        }

        binding.imvDelete.onSingleClick {
            if(countAudio == 0){
                Toast.makeText(requireContext(),getString(R.string.you_must_choose_1_file) , Toast.LENGTH_SHORT).show()
            }else {
                showDialogDelete()
            }
            if (countVideo == 0 ){
                Toast.makeText(requireContext(),getString(R.string.you_must_choose_1_file) , Toast.LENGTH_SHORT).show()
            }
            else{
                showDialogDelete()
            }

        }

        binding.imvRingtone.onSingleClick {
            if( countAudio == 0){
                Toast.makeText(requireContext(),getString(R.string.you_must_choose_1_file) , Toast.LENGTH_SHORT).show()
            }else {
                showDialogRingtone()
            }

            if (countVideo == 0 ){
                Toast.makeText(requireContext(),getString(R.string.you_must_choose_1_file) , Toast.LENGTH_SHORT).show()
            }
            else{
                showDialogRingtone()
            }

        }

        binding.imvRename.onSingleClick {
            if( countAudio == 0){
                Toast.makeText(requireContext(),getString(R.string.you_must_choose_1_file) , Toast.LENGTH_SHORT).show()
            }else {
                showDialogRename()
            }

            if(countVideo == 0){
                Toast.makeText(requireContext(),getString(R.string.you_must_choose_1_file) , Toast.LENGTH_SHORT).show()
            }
            else {
                showDialogRename()
            }
        }
    }

    private fun showDialogRingtone() {
        SystemUtils.setLocale(requireContext())
        val dialogBinding  = CustomDialogRingtoneBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = Dialog(requireContext())
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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
        val permission: Boolean = Settings.System.canWrite(requireContext())
        if (permission) {
            setRingtone(type)
        } else {
            openAndroidPermissionsMenu()
        }
    }

    private fun setRingtone(type: Int) {
        val customRingtoneUri = Uri.parse(listAudioStorage[positionAudioPlay].uri.toString())
        when (type) {
            0       -> {
                RingtoneManager.setActualDefaultRingtoneUri(requireContext(), RingtoneManager.TYPE_RINGTONE, customRingtoneUri)
            }

            1 -> {
                RingtoneManager.setActualDefaultRingtoneUri(requireContext(), RingtoneManager.TYPE_NOTIFICATION, customRingtoneUri)
            }

            2-> {
                RingtoneManager.setActualDefaultRingtoneUri(requireContext(), RingtoneManager.TYPE_ALARM, customRingtoneUri)
            }

            else                                                   -> {}
        }
        Toast.makeText(requireContext(), R.string.set_ringtone_successfully, Toast.LENGTH_SHORT).show()
    }


    private fun openAndroidPermissionsMenu() {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.setData(Uri.parse("package:" + requireContext().packageName))
        startActivity(intent)
    }

    private fun showDialogRename() {
        SystemUtils.setLocale(requireActivity())
        val dialogBinding  = CustomDialogRenameBinding.inflate(LayoutInflater.from(requireActivity()))
        val dialog = Dialog(requireContext())
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)
        dialogBinding.lnOk.onSingleClick{
            if(typefr.equals("vd")){
                val nameFile = dialogBinding.edtRename.text
                VideoUtils.renameVideoFile(requireContext(), listVideoStorage[positionVideoPlay].uri,
                    nameFile.toString())
                adapterVdFr.notifyDataSetChanged()
            }else{
                val nameFile = dialogBinding.edtRename.text
                AudioUtils.renameAudioFile(requireContext(),listAudioStorage[positionAudioPlay].uri,
                   nameFile.toString())
                listAudioStorage[positionAudioPlay].name = nameFile.toString()
                adapterFr.notifyDataSetChanged()
            }
            dialog.dismiss()
        }
        dialogBinding.lnCancel.onSingleClick {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showDialogDelete() {
        SystemUtils.setLocale(requireContext())
        val dialogBinding  = CustomeDialogDeleteBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = Dialog(requireContext())
        dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)
        dialogBinding.tvYes.onSingleClick{
            if(typefr.equals("vd")){
                for (i in 0..listVideoPick.size-1){
                    listVideoStorage.remove(listVideoPick[i])
                    adapterVdFr.notifyDataSetChanged()
                }
                deleteVideoList(listVideoPick)
                listVideoPick.clear()
                if(listVideoStorage.size == 0){
                    binding.lnNoItem.visibility = View.VISIBLE
                    binding.tvType.text = getString(R.string.you_don_t_have_any_content_yet_create_a_new_video_now)
                }else{
                    binding.lnNoItem.visibility = View.GONE
                }

                adapterVdFr.notifyDataSetChanged()
                countVideo = 0
                countSizeVideo = 0
                binding.tvSelectedItem.text = "$countVideo Selected"
                binding.size.text = "$countSizeVideo KB"
            }else{
                for (i in listAudioPick.size-1  downTo 0) {
                    listAudioStorage.remove(listAudioPick[i])
                    adapterFr.notifyDataSetChanged()
//                    val pos = listAudioPick[i].pos
//                    if (listAudioStorage[pos].active) {
//
//                        Log.d("check_logd_size", "showDialogDelete: ${listAudioStorage.size}")
//                    }
                }
                deleteAudioList(listAudioPick)
                listAudioPick.clear()
                adapterFr.notifyDataSetChanged()
                if(listAudioStorage.size == 0){
                    binding.lnNoItem.visibility = View.VISIBLE
                    binding.tvType.text = getString(R.string.you_don_t_have_any_content_yet_create_a_new_audio_now)
                }else{
                    binding.lnNoItem.visibility = View.GONE
                }
                Log.d("check_pos", "showDialogDelete: "+ listAudioPick)
                countAudio = 0
                countSize = 0
                binding.tvSelectedItem.text = "$countAudio Selected"
                binding.size.text = "$countSize KB"
            }
            dialog.dismiss()
        }
        dialogBinding.tvNo.onSingleClick {
            dialog.dismiss()
        }
        dialog.show()
    }
    private fun initRec() {
        adapterFr.getData(listAudioStorage)
        binding.recyclerViewTab2.adapter = adapterFr
    }

    fun deleteAudioList(audioList: List<AudioInfo>) {
        val contentResolver = context?.contentResolver
        audioList.forEach { audioInfo ->
            Log.d("DeleteFile", "File '${audioInfo.uri}' deleted successfully.")
            try {
                val rowsDeleted = contentResolver?.delete(audioInfo.uri, null, null)
                if (rowsDeleted != null) {
                    if (rowsDeleted > 0) {
                        Log.d("DeleteFile", "File '${audioInfo.name}' deleted successfully.")
                    } else {
                        Log.e("DeleteFile", "File '${audioInfo.name}' not found or could not be deleted.")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("DeleteFile", "Error deleting file '${audioInfo.name}': ${e.message}")
            }
        }
    }

    fun deleteVideoList(videoList: List<VideoInfo>) {
        val contentResolver = context?.contentResolver
        videoList.forEach { videoInfo ->
            Log.d("DeleteFile", "File '${videoInfo.uri}' deleted successfully.")
            try {
                val rowsDeleted = contentResolver?.delete(videoInfo.uri, null, null)
                if (rowsDeleted != null) {
                    if (rowsDeleted > 0) {
                        Log.d("DeleteFile", "File '${videoInfo.name}' deleted successfully.")
                    } else {
                        Log.e("DeleteFile", "File '${videoInfo.name}' not found or could not be deleted.")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("DeleteFile", "Error deleting file '${videoInfo.name}': ${e.message}")
            }
        }
    }

    fun deleteVideos(videoInfos: List<VideoInfo>): Boolean {
        var allDeleted = true
        videoInfos.forEach { videoInfo ->
            val file = File(videoInfo.uri.path ?: "")
            if (file.exists()) {
                val deleted = file.delete() // Xóa tệp
                if (!deleted) {
                    Log.e("DeleteVideos", "Failed to delete: ${videoInfo.name}")
                    allDeleted = false
                } else {
                    Log.d("DeleteVideos", "Deleted: ${videoInfo.name}")
                }
            } else {
                Log.e("DeleteVideos", "File does not exist: ${videoInfo.name}")
            }
        }

        return allDeleted // Trả về true nếu tất cả đã được xóa thành công
    }

    fun deleteAudioFiles(audioFiles: List<AudioInfo>): Boolean {
        var allDeleted = true
        audioFiles.forEach { audioFile ->
            val file = File(audioFile.uri.path ?: "")
            Log.e("DeleteAudioFiles", "Failed to delete: ${file}")
            if (file.exists()) {
                val deleted = file.delete()
                if (!deleted) {
//                    Log.e("DeleteAudioFiles", "Failed to delete: ${file.name}")
                    allDeleted = false
                } else {
//                    Log.d("DeleteAudioFiles", "Deleted: ${file.name}")
                }
            } else {
//                Log.e("DeleteAudioFiles", "File does not exist: ${file.name}")
            }
        }

        return allDeleted // Trả về true nếu tất cả đã được xóa thành công
    }

    private fun initData() {

        adapterVdFr.getData(listVideoStorage)
        binding.recyclerViewTab1.adapter = adapterVdFr
        binding.recyclerViewTab1.visibility = View.VISIBLE
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Video"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Audio"))
        if(listVideoStorage.isEmpty()){
            binding.lnNoItem.visibility = View.VISIBLE
            binding.tvType.text = getString(R.string.you_don_t_have_any_content_yet_create_a_new_audio_now)
        }else {
            binding.lnNoItem.visibility = View.GONE
        }
        Log.d("check_list_video", "initData: "+ listAudioStorage)
        if(typefr.equals("vd")){
            binding.imvRename.visibility = View.GONE
        }
        shareData = SharedPreferenceUtils.getInstance(requireContext())
        storageMusic = shareData.getStringValue("musicStorage").toString()
        storageVideo = shareData.getStringValue("videoStorage").toString()
    }

    fun renameAudioFile(context: Context, audioUri: Uri, newFileName: String): Boolean {
        val filePath = getRealPathFromURI(requireContext(), audioUri)
        Log.d("check_rename", "showDialogRename: kt" + filePath)
        if (filePath == null) {
            return false
        }
        val oldFile = File(filePath)
        val extension = oldFile.extension
        val newFile = File(oldFile.parent, "$newFileName.$extension")
        Log.d("check_rename", "showDialogRename: kt" + oldFile + " ____ " + newFile)
        if (newFile.exists()) {
            Log.d("check_rename", "showDialogRename: tbbbbbbbb")
            Toast.makeText(requireContext(), getString(R.string.the_file_already_exists_please_enter_a_different_name), Toast.LENGTH_SHORT).show()
            return false
        }
        Log.d("check_rename", "showDialogRename" + oldFile.renameTo(newFile))
        return oldFile.renameTo(newFile)
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
        adapterFr.notifyDataSetChanged()
        adapterVdFr.notifyDataSetChanged()
        isTouchEventHandled = false
        isClick = true
    }

}