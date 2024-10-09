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
import com.kan.dev.st_042_video_to_mp3.utils.AudioUtils
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.SystemUtils
import com.kan.dev.st_042_video_to_mp3.utils.VideoUtils
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.kan.dev.st_042_video_to_mp3.utils.showSystemUI
import com.kan.dev.st_042_video_to_mp3.view_model.SharedViewModel
import com.metaldetector.golddetector.finder.SharedPreferenceUtils
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity(), BottomNavVisibilityListener {
    lateinit var providerSharedPreference : SharedPreferenceUtils
    lateinit var binding: ActivityMainBinding
    private val viewModel: SharedViewModel by viewModels()
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
            shareAudioUris(this@MainActivity,Const.listAudioPick)
        }

        binding.lnCancel.onSingleClick {
            Const.isTouchEventHandled = false
            viewModel.triggerEvent()
            binding.btShare.visibility = View.GONE
            binding.bottomNavigationView.visibility = View.VISIBLE
        }
    }

    fun shareAudioUris(context: Context, listAudio: MutableList<AudioInfo>) {
        val uriList: ArrayList<Uri> = ArrayList()
        for (audio in listAudio) {
            uriList.add(audio.uri)
        }
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
            type = "audio/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Audio Files"))
    }

    private fun initData() {
        val navController = findNavController(R.id.nav_host_fragment)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        NavigationUI.setupWithNavController(bottomNavigationView, navController)
        providerSharedPreference = SharedPreferenceUtils.getInstance(this)

    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val navController = findNavController(R.id.nav_host_fragment)
        val currentDestination = navController.currentDestination?.id
        when (currentDestination) {
            R.id.homeFragment2 -> {
                Const.listVideoStorage.clear()
                Const.listVideoPick.clear()
                Const.listAudioPick.clear()
                Const.listAudioStorage.clear()
                var inAppCount = providerSharedPreference.getNumberRate("RateNumber")
                inAppCount ++
                providerSharedPreference.putNumber("RateNumber",inAppCount)
                Log.d("check_rate", "onBackPressed: "+ inAppCount + providerSharedPreference.getBooleanValue("booleanRate"))
                if(!providerSharedPreference.getBooleanValue("booleanRate")){
                    if( inAppCount % 2 == 0 ){
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
//                    finishAffinity()
                    if (isBackPress) {
                        exitProcess(0)
                    }
                }
            } else {
                Log.e("ReviewError", task.exception.toString());
//                finishAffinity()
                if (isBackPress) {
                    exitProcess(0)
                }
            }
        }
    }

    override fun onBottomNavVisibilityChanged(isVisible: Boolean) {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val bottomShare = findViewById<ConstraintLayout>(R.id.btShare)
        Log.d("check_visible", "onBottomNavVisibilityChanged: "+ isVisible)
        bottomNavigationView.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
        bottomShare.visibility = View.VISIBLE
    }
}