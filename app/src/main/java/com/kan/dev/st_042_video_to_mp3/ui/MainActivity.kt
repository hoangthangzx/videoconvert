package com.kan.dev.st_042_video_to_mp3.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.play.core.review.ReviewManagerFactory
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivityMainBinding
import com.kan.dev.st_042_video_to_mp3.databinding.DialogRateBinding
import com.kan.dev.st_042_video_to_mp3.interface_bottom.BottomNavVisibilityListener
import com.kan.dev.st_042_video_to_mp3.model.AudioInfo
import com.kan.dev.st_042_video_to_mp3.model.VideoInfo
import com.kan.dev.st_042_video_to_mp3.ui.fragment.HomeFragment
import com.kan.dev.st_042_video_to_mp3.ui.fragment.SettingFragment
import com.kan.dev.st_042_video_to_mp3.ui.fragment.storage.StorageFragment
import com.kan.dev.st_042_video_to_mp3.utils.AudioUtils
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoPick
import com.kan.dev.st_042_video_to_mp3.utils.Const.typefr
import com.kan.dev.st_042_video_to_mp3.utils.SystemUtils
import com.kan.dev.st_042_video_to_mp3.utils.VideoUtils
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.kan.dev.st_042_video_to_mp3.utils.showSystemUI
import com.kan.dev.st_042_video_to_mp3.view_model.SharedViewModel
import com.metaldetector.golddetector.finder.SharedPreferenceUtils
import java.io.File
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity(), BottomNavVisibilityListener {
    lateinit var providerSharedPreference : SharedPreferenceUtils
    lateinit var binding: ActivityMainBinding
    private val viewModel: SharedViewModel by viewModels()
    var currentDestination = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        SystemUtils.setLocale(this)
        showSystemUI(true)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initAction()
    }

    private fun initAction() {
        binding.lnShare.onSingleClick {
            Log.d("list_audio_pick", "initAction: "+ listAudioPick)
            if(typefr.equals("ad")){
                shareAudioFilesFromPaths(listAudioPick)
            }else{
                shareVideoFilesFromPaths(listVideoPick)
            }
            Const.isTouchEventHandled = false
            viewModel.triggerEvent()
            binding.btShare.visibility = View.GONE
            binding.bottomNavigationView.visibility = View.VISIBLE
        }

        binding.homeFragment.onSingleClick {
            currentDestination = 0
            binding.iconHome.isSelected = true
            binding.homeFragment.isSelected = true
            binding.storageFragment.isSelected = false
            binding.settingFragment.isSelected = false
            binding.iconStorage.isSelected = false
            binding.iconSetting.isSelected = false
            loadFragment(HomeFragment())
        }

        binding.storageFragment.onSingleClick {
            currentDestination = 1
            binding.iconHome.isSelected = false
            binding.iconStorage.isSelected = true
            binding.homeFragment.isSelected = false
            binding.storageFragment.isSelected = true
            binding.settingFragment.isSelected = false
            binding.iconSetting.isSelected = false
            loadFragment(StorageFragment())
        }

        binding.settingFragment.onSingleClick {
            currentDestination = 2
            binding.iconHome.isSelected = false
            binding.iconStorage.isSelected = false
            binding.homeFragment.isSelected = false
            binding.storageFragment.isSelected = false
            binding.settingFragment.isSelected = true
            binding.iconSetting.isSelected = true
            loadFragment(SettingFragment())
        }
        binding.lnCancel.onSingleClick {
            Const.isTouchEventHandled = false
            viewModel.triggerEvent()
            binding.btShare.visibility = View.GONE
            binding.bottomNavigationView.visibility = View.VISIBLE
        }


    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun shareAudioFilesFromPaths(audioInfoList: List<AudioInfo>) {
        val uriList = ArrayList<Uri>()

        audioInfoList.forEach { audioInfo ->
            val filePath = audioInfo.uri.toString().replace("file://", "")
            val file = File(filePath)
            val uri = FileProvider.getUriForFile(
                this,
                "${this.packageName}.provider",  // Package của ứng dụng bạn
                file
            )
            uriList.add(uri)
        }
        // Intent chia sẻ nhiều tệp
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "audio/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)  // Cấp quyền đọc URI
        }
        this@MainActivity.startActivity(Intent.createChooser(intent, "Share Audio Files"))
    }

    fun shareVideoFilesFromPaths(audioInfoList: List<VideoInfo>) {
        val uriList = ArrayList<Uri>()

        audioInfoList.forEach { videoInfo ->
            val filePath = videoInfo.uri.toString().replace("file://", "")
            val file = File(filePath)
            // Kiểm tra xem tệp có tồn tại không
            if (file.exists()) {
                val uri = FileProvider.getUriForFile(
                    this,
                    "${this.packageName}.provider",  // Package của ứng dụng bạn
                    file
                )
                uriList.add(uri)
            } else {
                Log.e("ShareVideo", "File not found: $filePath")
            }
        }

        if (uriList.isNotEmpty()) {
            // Intent chia sẻ nhiều tệp
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "video/*"  // Đặt kiểu MIME cho video
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)  // Cấp quyền đọc URI
            }
            this@MainActivity.startActivity(Intent.createChooser(intent, "Share Video Files"))
        } else {
            Log.e("ShareVideo", "No valid video files to share.")
        }
    }

//    fun shareAudioUris(context: Context, listAudio: MutableList<AudioInfo>) {
//        val uriList: ArrayList<Uri> = ArrayList()
//        for (audio in listAudio) {
//            uriList.add(audio.uri)
//        }
//        val shareIntent = Intent().apply {
//            action = Intent.ACTION_SEND_MULTIPLE
//            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
//            type = "audio/*"
//            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        }
//        context.startActivity(Intent.createChooser(shareIntent, "Share Audio Files"))
//    }

    private fun initData() {
        providerSharedPreference = SharedPreferenceUtils.getInstance(this)
        loadFragment(HomeFragment())
        binding.iconHome.isSelected = true
        binding.homeFragment.isSelected = true
        binding.storageFragment.isSelected = false
        binding.settingFragment.isSelected = false
        binding.iconStorage.isSelected = false
        binding.iconSetting.isSelected = false
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        when (currentDestination) {
            0-> {
//                Const.listVideoStorage.clear()
//                Const.listVideoPick.clear()
//                Const.listAudioPick.clear()
//                Const.listAudioStorage.clear()
                var inAppCount = providerSharedPreference.getNumberRate("RateNumber")
                inAppCount ++
                providerSharedPreference.putNumber("RateNumber",inAppCount)
                Log.d("check_rate", "onBackPressed: "+ inAppCount + providerSharedPreference.getBooleanValue("booleanRate"))
                if(!providerSharedPreference.getBooleanValue("booleanRate")){
                    if( inAppCount % 2 != 0 ){
                        val dialog = Dialog(this)
                        SystemUtils.setLocale(this)
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                        val bindingDialog = DialogRateBinding.inflate(layoutInflater)
                        dialog.setContentView(bindingDialog.root)
                        dialog.setCanceledOnTouchOutside(false)
                        dialog.setCancelable(false)
                        val window = dialog.window ?: return
                        window.setGravity(Gravity.CENTER)
                        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
                        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        bindingDialog.apply {
                            ll1.rating = 0f
                            ll1.setOnRatingChangeListener { _, p1, _ ->
                                if (p1.toInt() == 0) {
                                    tv1.text = getString(R.string.one_start_title)
                                    tv2.text = getString(R.string.one_start)
                                } else if (p1.toInt() in 1..3) {
                                    tv1.text = getString(R.string.one_start_title)
                                    tv2.text = getString(R.string.one_start)
                                } else {
                                    tv1.text = getString(R.string.four_start_title)
                                    tv2.text = getString(R.string.four_start)
                                }
                                when (p1.toInt()) {
                                    0 -> {
                                        imvAvtRate.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_rate_rero))
                                    }
                                    1 -> {
                                        imvAvtRate.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_rate_one))
                                    }
                                    2 -> {
                                        imvAvtRate.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_rate_two))
                                    }
                                    3 -> {
                                        imvAvtRate.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_rate_three))
                                    }
                                    4 -> {
                                        imvAvtRate.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_rate_four))
                                    }
                                    5 -> {
                                        imvAvtRate.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_rate_five))
                                    }
                                }
                            }
                            btnVote.onSingleClick {
                                providerSharedPreference.putBooleanValue("booleanRate", true)
                                if (ll1.rating.toInt() >= 3) {
                                    Toast.makeText(this@MainActivity, R.string.successful, Toast.LENGTH_SHORT).show()
                                    reviewApp(this@MainActivity, true)
                                    dialog.dismiss()
                                    finishAffinity()
                                } else if(ll1.rating.toInt() == 0){
                                    Toast.makeText(this@MainActivity, R.string.please_give_a_review, Toast.LENGTH_SHORT).show()
                                }else{
                                    dialog.dismiss()
                                    finishAffinity()
                                }
                            }
                            btnCancal.onSingleClick {
                                inAppCount += 1
                                providerSharedPreference.putBooleanValue("booleanRate",false)
                                dialog.dismiss()
                                finishAffinity()
                            }
                        }
                        dialog.show()
                    }else{
                        finishAffinity()
                    }
                }else{
                    finishAffinity()
                }
            }
            1 ->{
                finishAffinity()
            }
            2 -> {
                finishAffinity()
            }

        }
    }
    private fun reviewApp(context: Context, isBackPress: Boolean) {
        val manager = ReviewManagerFactory.create(context)
        val request = manager.requestReviewFlow();
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                Log.e("ReviewInfo", "" + reviewInfo.toString())
                val flow = (context as Activity?)?.let { manager.launchReviewFlow(it, reviewInfo) }
                flow?.addOnCompleteListener { task2: Task<Void> ->
                    Log.e("ReviewSuccess", task2.toString())
                    if (isBackPress) {
                        exitProcess(0)
                    }
                }
            } else {
                Log.e("ReviewError", task.exception.toString());
                if (isBackPress) {
                    exitProcess(0)
                }
            }
        }
    }
    override fun onBottomNavVisibilityChanged(isVisible: Boolean) {
        val linearLayout = findViewById<LinearLayout>(R.id.bottomNavigationView)
        val bottomShare = findViewById<ConstraintLayout>(R.id.btShare)
        Log.d("check_visible", "onBottomNavVisibilityChanged: "+ isVisible)
        linearLayout.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
        bottomShare.visibility = View.VISIBLE
    }
}