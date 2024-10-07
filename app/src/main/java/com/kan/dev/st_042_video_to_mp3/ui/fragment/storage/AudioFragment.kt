package com.kan.dev.st_042_video_to_mp3.ui.fragment.storage

import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.FragmentAudioBinding
import com.kan.dev.st_042_video_to_mp3.model.AudioInfo
import com.kan.dev.st_042_video_to_mp3.ui.ChooseItemStorageActivity
import com.kan.dev.st_042_video_to_mp3.ui.PlaySongActivity
import com.kan.dev.st_042_video_to_mp3.ui.audio_converter.AudioConverterAdapter
import com.kan.dev.st_042_video_to_mp3.ui.select_audio.SelectAudioAdapter
import com.kan.dev.st_042_video_to_mp3.utils.AudioUtils
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.Const.checkDataAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.checkType
import com.kan.dev.st_042_video_to_mp3.utils.Const.countAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSize
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioPick
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioFragment : Fragment() {
    lateinit var binding : FragmentAudioBinding
    var count = 0
    lateinit var adapterFr: AudioAdapterFr
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAudioBinding.inflate(layoutInflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initRec()
        initAction()
    }

    private fun initAction() {
        adapterFr.onClickListener(object : AudioAdapterFr.onClickItemListener{
            override fun onClickItem(position: Int, holder: AudioAdapterFr.ViewHolder) {
                Log.d("check_logg", "onClickEven:  9liulk8iku8l8ul")
                Const.positionAudioPlay = position
                startActivity(Intent(requireContext(),PlaySongActivity::class.java))
            }

            override fun onTouchEven(position: Int) {
                Log.d("check_logg", "onTouchEven:  ojkeeeee")
                checkType = false
                Const.positionAudioPlay = position
                listAudio[position].active = true
                listAudioPick.add(0, listAudio[position])
                countAudio += 1
                countSize += listAudio[position].sizeInMB.toInt()
                startActivity(Intent(requireContext(),ChooseItemStorageActivity::class.java))
            }
        })
    }

    private fun initRec() {
        adapterFr = AudioAdapterFr(requireContext())
        adapterFr.getData(listAudio)
        binding.recAudio.adapter = adapterFr
    }

    private fun initData() {
        if (!checkDataAudio){
            AudioUtils.getAllAudios(requireContext().contentResolver, listAudio)
            Log.d("check_list_video", "initData: "+ listAudio)
            checkDataAudio = true
        }
    }

    override fun onResume() {
        super.onResume()
        checkType = true
        countAudio = 0
        countSize = 0
    }

}