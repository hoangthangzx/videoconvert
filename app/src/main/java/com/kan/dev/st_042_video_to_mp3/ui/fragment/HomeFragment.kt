package com.kan.dev.st_042_video_to_mp3.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kan.dev.st_042_video_to_mp3.databinding.FragmentHomeBinding
import com.kan.dev.st_042_video_to_mp3.ui.select_audio.SelectAudioActivity
import com.kan.dev.st_042_video_to_mp3.ui.select_video.SelectVideoActivity
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.Const.checkType
import com.kan.dev.st_042_video_to_mp3.utils.Const.countAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSize
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSizeVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.countVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.musicStorage
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectType
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectTypeAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.videoStorage
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.SharedPreferenceUtils
import java.io.File


class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding
    lateinit var shareData : SharedPreferenceUtils

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initView()
        initAction()
    }

    private fun initView() {
        binding.tvConverter.isSelected = true
        binding.tvSpeed.isSelected = true
        binding.tvCutter.isSelected = true
        binding.tvMerger.isSelected = true
        binding.tvAudioConverter.isSelected = true
        binding.tvAudioCutter.isSelected = true
        binding.tvAudioMerger.isSelected = true
        binding.tvAudioSpeed.isSelected = true
    }

    private fun initData() {
        shareData = SharedPreferenceUtils.getInstance(requireContext())
        createMusicDirectoryOnce()
    }

    private fun createMusicDirectoryOnce() {
        val isDirectoryCreated = shareData.getBooleanValue("music")
        if (!isDirectoryCreated) {
            musicStorage = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "music")
            videoStorage = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "video")
            shareData.putStringValue("musicStorage", musicStorage.toString())
            shareData.putStringValue("videoStorage", videoStorage.toString())
            if (!musicStorage!!.exists() ) {
                if (musicStorage!!.mkdirs()) {
                    Log.d("MusicDir", "Directory created: ${musicStorage!!.path}")
                    shareData.putBooleanValue("music", true)
                } else {
                    Log.e("MusicDir", "Failed to create directory")
                }
            } else {
                Log.d("MusicDir", "Directory already exists: ${musicStorage!!.path}")
            }

            if (!videoStorage!!.exists() ) {
                if (videoStorage!!.mkdirs()) {
                    shareData.putBooleanValue("music", true)
                } else {
                    Log.e("MusicDir", "Failed to create directory")
                }
            } else {
                Log.d("MusicDir", "Directory already exists: ${musicStorage!!.path}")
            }
        }
    }

    private fun initAction() {
        binding.lnVideoMerger.onSingleClick {
            selectType = "VideoMerger"
            Const.checkData = false
            countVideo = 0
            countSizeVideo = 0
            startActivity(Intent(requireContext(),SelectVideoActivity::class.java))
        }

        binding.lnVideoConvert.onSingleClick {
            selectType = "Video"
            Const.checkData = false
            countVideo = 0
            countSizeVideo = 0
            startActivity(Intent(requireContext(), SelectVideoActivity::class.java))
        }

        binding.lnAudioConvert.onSingleClick {
            selectTypeAudio = "AudioConvert"
            countAudio = 0
            countSize = 0
            Const.checkDataAudio = false
            startActivity(Intent(requireContext(), SelectAudioActivity::class.java))
        }

        binding.lnVideoSpeed.onSingleClick {
            selectType = "Speed"
            Const.checkData = false
            countVideo = 0
            countSizeVideo = 0
            startActivity(Intent(requireContext(), SelectVideoActivity::class.java))
        }

        binding.lnAudioSpeed.onSingleClick {
            selectTypeAudio = "AudioSpeed"
            countAudio = 0
            countSize = 0
            Const.checkDataAudio = false
            startActivity(Intent(requireContext(), SelectAudioActivity::class.java))
        }

        binding.lnVideoCutter.onSingleClick {
            selectType = "VideoCutter"
            Const.checkData = false
            countVideo = 0
            countSizeVideo = 0
            startActivity(Intent(requireContext(), SelectVideoActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        countAudio = 0
        countSize = 0
        checkType = true
        selectType = ""
        selectTypeAudio = ""
        listVideo.clear()
        listVideoPick.clear()
    }
}