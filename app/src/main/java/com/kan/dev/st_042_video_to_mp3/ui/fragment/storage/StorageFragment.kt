package com.kan.dev.st_042_video_to_mp3.ui.fragment.storage
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.FragmentStorageBinding
import com.kan.dev.st_042_video_to_mp3.interface_bottom.BottomNavVisibilityListener
import com.kan.dev.st_042_video_to_mp3.ui.ChooseItemStorageActivity
import com.kan.dev.st_042_video_to_mp3.ui.PlaySongActivity
import com.kan.dev.st_042_video_to_mp3.utils.AudioUtils
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.Const.checkData
import com.kan.dev.st_042_video_to_mp3.utils.Const.checkDataAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.checkType
import com.kan.dev.st_042_video_to_mp3.utils.Const.countAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSize
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSizeVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.countVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.isTouchEventHandled
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionAudioPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionVideoPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.typefr
import com.kan.dev.st_042_video_to_mp3.utils.VideoUtils
import com.kan.dev.st_042_video_to_mp3.view_model.SharedViewModel

class StorageFragment : Fragment() {
    lateinit var binding : FragmentStorageBinding
    val adapterFr by lazy {
        AudioAdapterFr(requireContext())
    }

    val adapterVdFr by lazy {
        VideoAdapterFr(requireContext())
    }
    private val viewModel: SharedViewModel by activityViewModels()
    private var listener: BottomNavVisibilityListener? = null
    override fun onStart() {
        super.onStart()
        checkType = true
    }
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
                        if (!checkData){
                            VideoUtils.getAllVideos(requireContext().contentResolver)
                            Log.d("check_list_video", "initData: "+ listAudio)
                            checkData = true
                        }
                        binding.recyclerViewTab1.visibility = View.VISIBLE
                        binding.recyclerViewTab2.visibility = View.GONE
                    }
                    1 -> {
                        typefr ="ad"
                        if (!checkDataAudio){
                            AudioUtils.getAllAudios(requireContext().contentResolver)
                            Log.d("check_list_video", "initData: "+ listAudio)
                            checkDataAudio = true
                        }
                        initRec()
                        binding.recyclerViewTab1.visibility = View.GONE
                        binding.recyclerViewTab2.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        adapterVdFr.onClickListener(object : VideoAdapterFr.onClickItemListener{
            override fun onClickItem(position: Int, holder: VideoAdapterFr.ViewHolder) {
                if (isTouchEventHandled) {
                    if(!listVideo[position].active){
                        positionVideoPlay = position
                        countVideo += 1
                        countSizeVideo += listVideo[position].sizeInMB.toInt()
                        holder.binding.imvTickBox.setImageResource(R.drawable.icon_check_box_yes)
                        listVideoPick.add(0, listVideo[position])
                        listVideo[position].active = true
                    }else if(listVideo[position].active){
                        countVideo -= 1
                        holder.binding.imvTickBox.setImageResource(R.drawable.icon_check_box)
                        listVideo[position].active = false
                        listVideoPick.remove(listVideo[position])
                        countSizeVideo -= listVideo[position].sizeInMB.toInt()
                    }
                    if(countVideo > 1){
                        binding.imvRename.visibility = View.GONE
                        binding.imvRingtone.visibility = View.GONE
                    }else{
                        binding.imvRename.visibility = View.VISIBLE
                        binding.imvRingtone.visibility = View.VISIBLE
                    }

                    binding.tvSelectedItem.text = "$countVideo Selected"
                    binding.size.text = "$countSizeVideo KB"
                }else{
                    Log.d("check_logg", "onClickEven:  9liulk8iku8l8ul")
                    Const.positionAudioPlay = position
                    startActivity(Intent(requireContext(), PlaySongActivity::class.java))
                }
            }

            override fun onTouchEven(position: Int) {
                checkType = false
                isTouchEventHandled = true
                Const.positionVideoPlay = position
                listVideo[position].active = true
                listVideoPick.add(0, listVideo[position])
                countVideo += 1
                countSizeVideo += listVideo[position].sizeInMB.toInt()
                binding.ctlStorage.visibility = View.INVISIBLE
                binding.ctlItem.visibility = View.VISIBLE
                listener?.onBottomNavVisibilityChanged(false)
                if(countVideo > 1){
                    binding.imvRename.visibility = View.GONE
                    binding.imvRingtone.visibility = View.GONE
                }else{
                    binding.imvRename.visibility = View.VISIBLE
                    binding.imvRingtone.visibility = View.VISIBLE
                }
                binding.tvSelectedItem.text = "$countVideo Selected"
                binding.size.text = "$countSizeVideo KB"
                adapterVdFr.notifyDataSetChanged()
            }

        })


        adapterFr.onClickListener(object : AudioAdapterFr.onClickItemListener{
            override fun onClickItem(position: Int, holder: AudioAdapterFr.ViewHolder) {
                if (isTouchEventHandled) {
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
                }else{
                    Log.d("check_logg", "onClickEven:  9liulk8iku8l8ul")
                    Const.positionAudioPlay = position
                    startActivity(Intent(requireContext(), PlaySongActivity::class.java))
                }

            }

            override fun onTouchEven(position: Int) {
                Log.d("check_logg", "onTouchEven:  ojkeeeee")
                checkType = false
                isTouchEventHandled = true
                Const.positionAudioPlay = position
                listAudio[position].active = true
                listAudioPick.add(0, listAudio[position])
                countAudio += 1
                countSize += listAudio[position].sizeInMB.toInt()
                binding.ctlStorage.visibility = View.INVISIBLE
                binding.ctlItem.visibility = View.VISIBLE
                listener?.onBottomNavVisibilityChanged(false)
                if(countAudio > 1){
                    binding.imvRename.visibility = View.GONE
                    binding.imvRingtone.visibility = View.GONE
                }else{
                    binding.imvRename.visibility = View.VISIBLE
                    binding.imvRingtone.visibility = View.VISIBLE
                }
                binding.tvSelectedItem.text = "$countAudio Selected"
                binding.size.text = "$countSize KB"
                adapterFr.notifyDataSetChanged()
            }
        })

        viewModel.eventTriggered.observe(viewLifecycleOwner) { triggered ->
            if (triggered) {
                if(typefr.equals("vd")){
                    checkType = true
                    listAudioPick.clear()
                    listAudio.clear()
                    AudioUtils.getAllAudios(requireContext().contentResolver)
                    countAudio = 0
                    countSize = 0
                    adapterFr.notifyDataSetChanged()
                }else{
                    checkType = true
                    listVideoPick.clear()
                    listVideo.clear()
                    VideoUtils.getAllVideos(requireContext().contentResolver)
                    countVideo = 0
                    countSizeVideo = 0
                    adapterVdFr.notifyDataSetChanged()
                }
                viewModel.resetEvent()
                binding.ctlStorage.visibility = View.VISIBLE
                binding.ctlItem.visibility = View.GONE
            }
        }
    }

    private fun initRec() {
        adapterFr.getData(listAudio)
        binding.recyclerViewTab2.adapter = adapterFr
        adapterVdFr.getData(listVideo)
        binding.recyclerViewTab1.adapter = adapterVdFr

    }

    private fun initData() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Video"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Audio"))
        VideoUtils.getAllVideos(requireContext().contentResolver)
        adapterVdFr.getData(listVideo)
        binding.recyclerViewTab1.adapter = adapterVdFr
        Log.d("check_list_video", "initData: "+ listAudio)
        if (!checkData){

            checkData = true
        }
    }

}