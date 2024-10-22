package com.kan.dev.st_042_video_to_mp3.ui

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivityAboutUsBinding
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.AbsBaseActivity

class ActivityAboutUs : AbsBaseActivity<ActivityAboutUsBinding>(false) {
    override fun getFragmentID(): Int  = 0
    override fun getLayoutId(): Int = R.layout.activity_about_us
    var versionName =""
    override fun init() {
        initView()
        initAction()
    }

    private fun initAction() {
        binding.imvBack.onSingleClick {
            finish()
        }
    }

    private fun initView() {
        try {
            val packageManager: PackageManager = this.packageManager
            val packageInfo: PackageInfo = packageManager.getPackageInfo(this.packageName, 0)
            versionName = packageInfo.versionName

        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        binding.tvVersion.text = "${getString(R.string.version)} $versionName"
    }
}