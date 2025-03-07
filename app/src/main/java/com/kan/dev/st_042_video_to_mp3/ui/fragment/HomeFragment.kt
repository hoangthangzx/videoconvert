package com.kan.dev.st_042_video_to_mp3.ui.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewManagerFactory
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.DialogRateBinding
import com.kan.dev.st_042_video_to_mp3.databinding.FragmentHomeBinding
import com.kan.dev.st_042_video_to_mp3.model.AudioSpeedModel
import com.kan.dev.st_042_video_to_mp3.ui.ActivityAboutUs
import com.kan.dev.st_042_video_to_mp3.ui.select_audio.SelectAudioActivity
import com.kan.dev.st_042_video_to_mp3.ui.select_video.SelectVideoActivity
import com.kan.dev.st_042_video_to_mp3.utils.AudioUtils
import com.kan.dev.st_042_video_to_mp3.utils.AudioUtils.countPos
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.Const.audioCutter
import com.kan.dev.st_042_video_to_mp3.utils.Const.audioInfo
import com.kan.dev.st_042_video_to_mp3.utils.Const.checkBoolean
import com.kan.dev.st_042_video_to_mp3.utils.Const.checkData
import com.kan.dev.st_042_video_to_mp3.utils.Const.checkType
import com.kan.dev.st_042_video_to_mp3.utils.Const.countAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.countMap
import com.kan.dev.st_042_video_to_mp3.utils.Const.countPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSize
import com.kan.dev.st_042_video_to_mp3.utils.Const.countSizeVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.countVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.elementCounts
import com.kan.dev.st_042_video_to_mp3.utils.Const.isTouchEventHandled
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioPickMerger
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioSaved
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioStorage
import com.kan.dev.st_042_video_to_mp3.utils.Const.listConvertMp3
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoStorage
import com.kan.dev.st_042_video_to_mp3.utils.Const.musicStorage
import com.kan.dev.st_042_video_to_mp3.utils.Const.selecMerger
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectFr
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectType
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectTypeAudio
import com.kan.dev.st_042_video_to_mp3.utils.Const.videoCutter
import com.kan.dev.st_042_video_to_mp3.utils.Const.videoStorage
import com.kan.dev.st_042_video_to_mp3.utils.SystemUtils
import com.kan.dev.st_042_video_to_mp3.utils.VideoUtils
import com.kan.dev.st_042_video_to_mp3.utils.VideoUtils.countVd
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.SharedPreferenceUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.system.exitProcess

class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding
    lateinit var shareData : SharedPreferenceUtils

    var storageMusic = ""
    var storageVideo = ""
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
        binding.tvCutter.isSelected = true
        binding.tvMerger.isSelected = true
        binding.tvAudioConverter.isSelected = true
        binding.tvAudioCutter.isSelected = true
        binding.tvAudioMerger.isSelected = true
        binding.tvAudioSpeed.isSelected = true
    }

    private fun initData() {
        shareData = SharedPreferenceUtils.getInstance(requireContext())
        storageMusic = shareData.getStringValue("musicStorage").toString()
        storageVideo = shareData.getStringValue("videoStorage").toString()
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
        binding.imvAboutUs.onSingleClick {
            startActivity(Intent(requireContext(),ActivityAboutUs::class.java))
        }
//        binding.lnAudioMerger.onSingleClick {
//            selectTypeAudio = "AudioMerger"
//            checkBoolean = "AudioMerger"
//            countAudio = 0
//            countSize = 0
//            listAudioStorage.clear()
//            Const.checkDataAudio = false
//            startActivity(Intent(requireContext(), SelectAudioActivity::class.java))
//        }

        binding.lnVideoConvertToMp3.onSingleClick {
            selectType = "VideoConvert"
            Const.checkData = false
            countVideo = 0
            countSizeVideo = 0
            listVideoStorage.clear()
            startActivity(Intent(requireContext(),SelectVideoActivity::class.java))
        }
//        binding.lnVideoMerger.onSingleClick {
//            selectType = "VideoMerger"
//            Const.checkData = false
//            countVideo = 0
//            countSizeVideo = 0
//            startActivity(Intent(requireContext(),SelectVideoActivity::class.java))
//        }
        binding.lnVideoConvert.onSingleClick {
//            listVideo.clear()
//            VideoUtils.getAllVideos(requireContext().contentResolver)
            selectTypeAudio = "Video"
            Const.checkData = false
            countVideo = 0
            countSizeVideo = 0
            listVideoStorage.clear()
            startActivity(Intent(requireContext(), SelectVideoActivity::class.java))
        }

        binding.lnAudioConvert.onSingleClick {
            listAudio.clear()
            AudioUtils.getAllAudios(requireContext().contentResolver)
            selectTypeAudio = "AudioConvert"
            countAudio = 0
            countSize = 0
            listAudioStorage.clear()
            Const.checkDataAudio = false
            startActivity(Intent(requireContext(), SelectAudioActivity::class.java))
        }

        binding.lnAudioMerger.onSingleClick {
            listAudio.clear()
            AudioUtils.getAllAudios(requireContext().contentResolver)
            selectTypeAudio = "AudioMerger"
            selecMerger = true
            countAudio = 0
            countSize = 0
//            listAudio.clear()
            listAudioStorage.clear()
            Const.checkDataAudio = false
            startActivity(Intent(requireContext(), SelectAudioActivity::class.java))
        }

        binding.lnAudioCutter.onSingleClick {
            listAudio.clear()
            AudioUtils.getAllAudios(requireContext().contentResolver)
            selectTypeAudio = "AudioCutter"
            countAudio = 0
            countSize = 0
            listAudioStorage.clear()
            Const.checkDataAudio = false
            startActivity(Intent(requireContext(), SelectAudioActivity::class.java))
        }
//        binding.lnVideoSpeed.onSingleClick {
//            selectType = "Speed"
//            Const.checkData = false
//            countVideo = 0
//            countSizeVideo = 0
//            startActivity(Intent(requireContext(), SelectVideoActivity::class.java))
//        }


        binding.lnAudioSpeed.onSingleClick {
            listAudio.clear()
            AudioUtils.getAllAudios(requireContext().contentResolver)
            selectTypeAudio = "AudioSpeed"
            countAudio = 0
            countSize = 0
            listAudioStorage.clear()
            Const.checkDataAudio = false
            startActivity(Intent(requireContext(), SelectAudioActivity::class.java))
        }

        binding.lnVideoCutter.onSingleClick {
            selectType = "VideoCutter"
            Const.checkData = false
            countVideo = 0
            countSizeVideo = 0
            listVideoStorage.clear()
            startActivity(Intent(requireContext(), SelectVideoActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        initDataItem()
    }

    private fun initDataItem() {
        countMap.clear()
//        listAudioStorage.clear()
//        getAllAudiosFromSpecificDirectory_1(storageMusic)
        audioCutter = null
        videoCutter = null
        countPos = 0
        countVd = 0
        countAudio = 0
        countSize = 0
        countPlay = 0
        checkType = true
        selectType = ""
        selectFr = ""
        checkBoolean = ""
        selecMerger = false
        isTouchEventHandled = false
        selectTypeAudio = ""
        listVideoPick.clear()
        listAudioPick.clear()
        listAudioSaved.clear()
        listConvertMp3.clear()
        listAudioPickMerger.clear()
        audioInfo  = null
        Log.d("check_logddd", "onResume: co nhay vap")
        Log.d("check_data", "onResume: "+  storageVideo  + "    " + listVideoStorage)
    }
}