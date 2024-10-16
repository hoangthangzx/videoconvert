package com.kan.dev.st_042_video_to_mp3.ui.fragment.storage
import android.app.Dialog
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.media3.common.util.UnstableApi
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.CustomDialogRingtoneBinding
import com.kan.dev.st_042_video_to_mp3.databinding.FragmentStorageBinding
import com.kan.dev.st_042_video_to_mp3.interface_bottom.BottomNavVisibilityListener
import com.kan.dev.st_042_video_to_mp3.ui.ActivityAboutUs
import com.kan.dev.st_042_video_to_mp3.ui.PlaySongActivity
import com.kan.dev.st_042_video_to_mp3.ui.PlayVideoActivity
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.Const.audioInformation
import com.kan.dev.st_042_video_to_mp3.utils.Const.checkType
import com.kan.dev.st_042_video_to_mp3.utils.Const.countAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSize
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSizeVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.countVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.currentRingtone
import com.kan.dev.st_042_video_to_mp3.utils.Const.isTouchEventHandled
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioStorage
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoStorage
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionAudioPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionVideoPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.uriPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.videoInfo
import com.kan.dev.st_042_video_to_mp3.utils.SystemUtils
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.kan.dev.st_042_video_to_mp3.view_model.SharedViewModel
import com.metaldetector.golddetector.finder.SharedPreferenceUtils


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
        binding.imvAboutUs.onSingleClick {
            startActivity(Intent(requireContext(), ActivityAboutUs::class.java))
        }
        binding.imvAll.onSingleClick {
            binding.imvAllTrue.visibility = View.VISIBLE
            binding.imvAll.visibility = View.GONE
            listAudioStorage.forEach{it.active = true}
            adapterFr.notifyDataSetChanged()
            listAudioPick = listAudioStorage.map { it.copy() }.toMutableList()
        }

        binding.imvAllTrue.onSingleClick {
            binding.imvAllTrue.visibility = View.GONE
            binding.imvAll.visibility = View.VISIBLE
            listAudioStorage.forEach{it.active = false}
            adapterFr.notifyDataSetChanged()
            listAudioPick.clear()
        }

        adapterVdFr.onClickListener(object : VideoAdapterFr.onClickItemListener{
            @OptIn(UnstableApi::class)
            override fun onClickItem(position: Int, holder: VideoAdapterFr.ViewHolder) {
                if (isTouchEventHandled  &&  isClick == true) {
                    if(!listVideoStorage[position].active){
                        positionVideoPlay = position
                        countVideo += 1
                        countSizeVideo += listVideoStorage[position].sizeInMB.toInt()
                        holder.binding.imvTickBox.setImageResource(R.drawable.icon_check_box_yes)
                        listVideoPick.add(listVideoStorage[position])
                        listVideoStorage[position].active = true
                    }else if(listVideoStorage[position].active){
                        countVideo -= 1
                        holder.binding.imvTickBox.setImageResource(R.drawable.icon_check_box)
                        listVideoStorage[position].active = false
                        listVideoPick.removeAt(position)
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
                    positionVideoPlay = position
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
                    positionAudioPlay = position
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
                    binding.imvAllTrue.visibility = View.GONE
                    binding.imvAll.visibility = View.VISIBLE
                    isTouchEventHandled = false
                    checkType = true
                    countAudio = 0
                    countSize = 0
                    countSizeVideo = 0
                    countVideo = 0
                    checkType = true
                    viewModel.resetEvent()
                    listAudioPick.clear()
                    listAudioStorage.forEach { it.active = false }
                    adapterFr.notifyDataSetChanged()
                    binding.ctlStorage.visibility = View.VISIBLE
                    binding.ctlItem.visibility = View.GONE
                    binding.tvSelectedItem.text = "$countVideo Selected"
                    binding.size.text = "$countSizeVideo KB"
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

    private fun initRec() {
        adapterFr.getData(listAudioStorage)
        binding.recyclerViewTab2.adapter = adapterFr
    }

    private fun initData() {
        initRec()
        shareData = SharedPreferenceUtils.getInstance(requireContext())
        storageMusic = shareData.getStringValue("musicStorage").toString()
        storageVideo = shareData.getStringValue("videoStorage").toString()
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
        isClick = false
    }

}