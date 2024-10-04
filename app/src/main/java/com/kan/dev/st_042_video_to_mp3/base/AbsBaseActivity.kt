package com.metaldetector.golddetector.finder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.kan.dev.st_042_video_to_mp3.utils.SystemUtils
import com.kan.dev.st_042_video_to_mp3.utils.showSystemUI


abstract class AbsBaseActivity<V : ViewDataBinding>(var fragment : Boolean) : AppCompatActivity() {
    lateinit var binding: V
    lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SystemUtils.setLocale(this)
        binding = DataBindingUtil.setContentView(this, getLayoutId())
        if(fragment){
            navHostFragment =
                supportFragmentManager.findFragmentById(getFragmentID()) as NavHostFragment
            navController = navHostFragment.navController
        }
        init()
    }
    override fun onResume() {
        super.onResume()
        showSystemUI(true)
    }
    abstract fun getFragmentID(): Int
    abstract fun getLayoutId(): Int
    abstract fun init()
}