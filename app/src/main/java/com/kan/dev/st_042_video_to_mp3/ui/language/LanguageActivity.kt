package com.kan.dev.st_042_video_to_mp3.ui.language

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivityLanguageBinding
import com.kan.dev.st_042_video_to_mp3.ui.MainActivity
import com.kan.dev.st_042_video_to_mp3.ui.intro.IntroActivity
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.Const.listLanguage
import com.kan.dev.st_042_video_to_mp3.utils.SystemUtils
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.AbsBaseActivity
import com.metaldetector.golddetector.finder.SharedPreferenceUtils
import com.metaldetector.golddetector.finder.model.LanguageModel

class LanguageActivity : AbsBaseActivity<ActivityLanguageBinding>(false) {
    override fun getFragmentID(): Int = 0

    override fun getLayoutId(): Int = R.layout.activity_language

    lateinit var adapter: LanguageAdapter
    lateinit var providerSharedPreference: SharedPreferenceUtils
    var codeLang: String? = null

    override fun init() {
        providerSharedPreference = SharedPreferenceUtils.getInstance(this)
        codeLang = providerSharedPreference.getStringValue("language")
        Log.d("check_code_lang", "setClick: "+ codeLang)
        if (codeLang.equals("")) {
            providerSharedPreference.putStringValue("language", "")
            codeLang = ""
        }
        val state = providerSharedPreference.getStringValue("stateLanguage")
        if(state.equals("on")){
            binding.lnStatus.visibility= View.GONE
            binding.lnStatus2.visibility = View.VISIBLE
        }
        adapter = LanguageAdapter(this@LanguageActivity)
        setRecycleView()
        setClick()
        initView()
    }

    private fun initView() {
    }
    private fun setClick() {
        binding.imvBack.onSingleClick {
            finish()
        }

        binding.imvTick.onSingleClick {
            SystemUtils.setPreLanguage(this, codeLang)
            providerSharedPreference.putStringValue("language", codeLang)
            if (SharedPreferenceUtils.getInstance(this)
                    .getBooleanValue(Const.LANGUAGE)
            ) {
                val intent = Intent(
                    this,
                    MainActivity::class.java
                )
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                finishAffinity()
                startActivity(intent)
            } else {
                SharedPreferenceUtils.getInstance(this)
                    .putBooleanValue(Const.LANGUAGE, true)
                val intent = Intent(this, IntroActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        binding.imvText.onSingleClick {
            Log.d("check_code_lang", "setClick: "+ codeLang)
            if(codeLang.equals("")){
                Toast.makeText(this@LanguageActivity, "Select Language", Toast.LENGTH_SHORT).show()
            }else{
                providerSharedPreference.putStringValue("stateLanguage","on")
                SystemUtils.setPreLanguage(applicationContext, codeLang)
                providerSharedPreference.putStringValue("language", codeLang)
                if (SharedPreferenceUtils.getInstance(applicationContext)
                        .getBooleanValue(Const.LANGUAGE)
                ) {
                    var intent = Intent(
                        this@LanguageActivity,
                        MainActivity::class.java
                    )
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                    finishAffinity()
                    startActivity(intent)
                } else {
                    SharedPreferenceUtils.getInstance(applicationContext)
                        .putBooleanValue(Const.LANGUAGE, true)
                    var intent = Intent(this@LanguageActivity, IntroActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
    private fun setRecycleView() {
        Log.d("check_code_lang", "rec: "+ codeLang)
        var i = 0
        lateinit var x: LanguageModel
        if (!codeLang.equals("")) {
            listLanguage.forEach {
                listLanguage[i].active = false
                if (codeLang.equals(it.code)) {
                    x = listLanguage[i]
                    x.active = true
                }
                i++
            }
            listLanguage.remove(x)
            listLanguage.add(0, x)
        }
        adapter.getData(listLanguage)
        binding.recLanguage.adapter = adapter
        val manager = GridLayoutManager(applicationContext, 1, RecyclerView.VERTICAL, false)
        binding.recLanguage.layoutManager = manager

        adapter.onClick = {
            codeLang = listLanguage[it].code
        }
    }



    override fun onBackPressed() {
        listLanguage.forEach {
            it.active = false
        }
        super.onBackPressed()
        finishAffinity()
    }
}