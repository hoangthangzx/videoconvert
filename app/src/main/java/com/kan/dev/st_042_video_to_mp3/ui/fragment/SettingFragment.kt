package com.kan.dev.st_042_video_to_mp3.ui.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewManagerFactory
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.DialogRateBinding
import com.kan.dev.st_042_video_to_mp3.databinding.FragmentSettingBinding
import com.kan.dev.st_042_video_to_mp3.ui.language.LanguageActivity
import com.kan.dev.st_042_video_to_mp3.utils.SystemUtils
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.SharedPreferenceUtils
import kotlin.system.exitProcess


class SettingFragment : Fragment() {
    lateinit var binding: FragmentSettingBinding
    lateinit var providerSharedPreference : SharedPreferenceUtils
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingBinding.inflate(inflater,container,false)
        return binding.root
    }
    var storageMusic = ""
    var storageVideo = ""
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initView()
        initAction()
    }
    private fun initData() {
        providerSharedPreference = SharedPreferenceUtils.getInstance(requireContext())
        storageMusic = providerSharedPreference.getStringValue("musicStorage").toString()
        storageVideo = providerSharedPreference.getStringValue("videoStorage").toString()
    }

    private fun initAction() {
        binding.lnLanguage.onSingleClick {
            val intent = Intent(requireContext(), LanguageActivity::class.java)
            startActivity(intent)
        }

//        binding.lnRate.onSingleClick {
//            val dialog = Dialog(activityC)
//            SystemUtils.setLocale(requireActivity())
//            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//            val bindingDialog = DialogRateBinding.inflate(layoutInflater)
//            dialog.setContentView(bindingDialog.root)
//            dialog.setCanceledOnTouchOutside(false)
//            dialog.setCancelable(false)
//            val window = dialog.window ?: return@onSingleClick
//            window.setGravity(Gravity.CENTER)
//            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
//            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//            bindingDialog.apply{
//                ll1.rating = 0f
//                ll1.setOnRatingChangeListener { _, p1, _ ->
//                    if (p1.toInt() == 0) {
//                        tv1.text = getString(R.string.zero_start_title)
//                        tv2.text = getString(R.string.zero_start)
//                    } else if (p1.toInt() in 1..3) {
//                        tv1.text = getString(R.string.one_start_title)
//                        tv2.text = getString(R.string.one_start)
//                    } else {
//                        tv1.text = getString(R.string.four_start_title)
//                        tv2.text = getString(R.string.four_start)
//                    }
//                    when (p1.toInt()) {
//                        0 -> {
//                            imvAvtRate.setImageDrawable(
//                                ContextCompat.getDrawable(requireActivity(),
//                                R.drawable.ic_rate_rero
//                            ))
//                        }
//                        1 -> {
//                            imvAvtRate.setImageDrawable(
//                                ContextCompat.getDrawable(requireActivity(),
//                                R.drawable.ic_rate_one
//                            ))
//                        }
//                        2 -> {
//                            imvAvtRate.setImageDrawable(
//                                ContextCompat.getDrawable(requireActivity(),
//                                R.drawable.ic_rate_two
//                            ))
//                        }
//                        3 -> {
//                            imvAvtRate.setImageDrawable(
//                                ContextCompat.getDrawable(requireActivity(),
//                                R.drawable.ic_rate_three
//                            ))
//                        }
//                        4 -> {
//                            imvAvtRate.setImageDrawable(
//                                ContextCompat.getDrawable(requireActivity(),
//                                R.drawable.ic_rate_four
//                            ))
//                        }
//                        5 -> {
//                            imvAvtRate.setImageDrawable(
//                                ContextCompat.getDrawable(requireActivity(),
//                                R.drawable.ic_rate_five
//                            ))
//                        }
//                    }
//                }
//                btnVote.onSingleClick {
//                    if (ll1.rating.toInt() > 3) {
//                        Toast.makeText(requireActivity(), R.string.successful, Toast.LENGTH_SHORT).show()
//                        reviewApp(requireActivity(), true)
//                        binding.lnRate.visibility = View.GONE
//                        providerSharedPreference.putBooleanValue("booleanRate", true)
//                        binding.lnRate.visibility = View.GONE
//                        dialog.dismiss()
//                    } else if(ll1.rating.toInt() == 0){
//                        Toast.makeText(requireActivity(), R.string.please_give_a_review, Toast.LENGTH_SHORT).show()
//                    }else{
//                        providerSharedPreference.putBooleanValue("booleanRate", true)
//                        dialog.dismiss()
//                        binding.lnRate.visibility = View.GONE
//                    }
//                }
//                btnCancal.onSingleClick {
//                    dialog.dismiss()
//                }
//            }
//            dialog.show()
//        }

        binding.lnRate.onSingleClick {
            activity?.let { activityContext ->
                val dialog = Dialog(activityContext)
                SystemUtils.setLocale(activityContext)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                val bindingDialog = DialogRateBinding.inflate(layoutInflater)
                dialog.setContentView(bindingDialog.root)
                dialog.setCanceledOnTouchOutside(false)
                dialog.setCancelable(false)

                val window = dialog.window ?: return@onSingleClick
                window.setGravity(Gravity.CENTER)
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

                bindingDialog.apply {
                    ll1.rating = 0f
                    ll1.setOnRatingChangeListener { _, p1, _ ->
                        if (p1.toInt() == 0) {
                            tv1.text = getString(R.string.zero_start_title)
                            tv2.text = getString(R.string.zero_start)
                        } else if (p1.toInt() in 1..3) {
                            tv1.text = getString(R.string.one_start_title)
                            tv2.text = getString(R.string.one_start)
                        } else {
                            tv1.text = getString(R.string.four_start_title)
                            tv2.text = getString(R.string.four_start)
                        }
                        when (p1.toInt()) {
                            0 -> imvAvtRate.setImageDrawable(ContextCompat.getDrawable(activityContext, R.drawable.ic_rate_rero))
                            1 -> imvAvtRate.setImageDrawable(ContextCompat.getDrawable(activityContext, R.drawable.ic_rate_one))
                            2 -> imvAvtRate.setImageDrawable(ContextCompat.getDrawable(activityContext, R.drawable.ic_rate_two))
                            3 -> imvAvtRate.setImageDrawable(ContextCompat.getDrawable(activityContext, R.drawable.ic_rate_three))
                            4 -> imvAvtRate.setImageDrawable(ContextCompat.getDrawable(activityContext, R.drawable.ic_rate_four))
                            5 -> imvAvtRate.setImageDrawable(ContextCompat.getDrawable(activityContext, R.drawable.ic_rate_five))
                        }
                    }

                    btnVote.onSingleClick {
                        if (ll1.rating.toInt() > 3) {
                            Toast.makeText(activityContext, R.string.successful, Toast.LENGTH_SHORT).show()
                            reviewApp(activityContext, true)
                            binding.lnRate.visibility = View.GONE
                            providerSharedPreference.putBooleanValue("booleanRate", true)
                            binding.lnRate.visibility = View.GONE
                            dialog.dismiss()
                        } else if(ll1.rating.toInt() == 0){
                            Toast.makeText(activityContext, R.string.please_give_a_review, Toast.LENGTH_SHORT).show()
                        } else {
                            providerSharedPreference.putBooleanValue("booleanRate", true)
                            dialog.dismiss()
                            binding.lnRate.visibility = View.GONE
                        }
                    }

                    btnCancal.onSingleClick {
                        dialog.dismiss()
                    }
                }
                dialog.show()
            }
        }


        binding.lnShare.onSingleClick {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                var shareMessage = "Download application: "
                shareMessage = (shareMessage + "https://play.google.com/store/apps/details?id=" + requireContext().packageName)
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                startActivity(Intent.createChooser(shareIntent, "Share with"))
            } catch (e: Exception) {
            }
        }
        binding.lnPolicy.onSingleClick {
            val url = "https://sites.google.com/view/lvt-policy-gold-finder/"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
    }
    private fun initView() {
        binding.tvPathAudio.text = storageMusic
        binding.tvPathVideo.text = storageVideo

        if (providerSharedPreference.getBooleanValue("booleanRate")
        ) {
            binding.lnRate.visibility = View.GONE
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
//                        exitProcess(0)
                    }
                }
            } else {
                Log.e("ReviewError", task.exception.toString());
//                finishAffinity()
                if (isBackPress) {
//                    exitProcess(0)
                }
            }
        }
    }


}