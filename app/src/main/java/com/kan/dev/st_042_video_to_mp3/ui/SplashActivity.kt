package com.kan.dev.st_042_video_to_mp3.ui

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivitySpalshBinding
import com.kan.dev.st_042_video_to_mp3.ui.intro.IntroActivity
import com.kan.dev.st_042_video_to_mp3.ui.language.LanguageActivity
import com.kan.dev.st_042_video_to_mp3.utils.Const.SPLASH_DELAY
import com.metaldetector.golddetector.finder.AbsBaseActivity
import com.metaldetector.golddetector.finder.SharedPreferenceUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AbsBaseActivity<ActivitySpalshBinding>(false) {
    override fun getFragmentID(): Int  = 0

    override fun getLayoutId(): Int = R.layout.activity_spalsh
    var check_state : String? = null
    lateinit var providerSharedPreference: SharedPreferenceUtils
    override fun init() {
        if ((!isTaskRoot && intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intent.action != null) && intent.action == Intent.ACTION_MAIN
        ) {    finish()
            return
        }
        initData()
        initAction()
    }

    private fun initAction() {
        providerSharedPreference = SharedPreferenceUtils.getInstance(this@SplashActivity)
        check_state = providerSharedPreference.getStringValue("stateLanguage")
    }

    private fun initData() {
        val animator = ObjectAnimator.ofInt(binding.lottieAnimationView, "progress", 0, 100)
        animator.duration = 3000 // Thời gian chạy animation (5 giây)
        animator.start()
        CoroutineScope(Dispatchers.Main).launch {
            val delayMillis = SPLASH_DELAY
            delay(delayMillis)
            startActivity(Intent(this@SplashActivity, LanguageActivity::class.java))
            if (check_state.equals("on")) {
                startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, LanguageActivity::class.java))
            }
            finish()
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
    }

}