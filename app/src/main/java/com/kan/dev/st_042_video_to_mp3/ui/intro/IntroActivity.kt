package com.kan.dev.st_042_video_to_mp3.ui.intro

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivityIntroBinding
import com.kan.dev.st_042_video_to_mp3.ui.MainActivity
import com.kan.dev.st_042_video_to_mp3.ui.PermissionActivity
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.SystemUtils
import com.kan.dev.st_042_video_to_mp3.utils.dpToPx
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.kan.dev.st_042_video_to_mp3.utils.showSystemUI
import com.metaldetector.golddetector.finder.AbsBaseActivity
import com.metaldetector.golddetector.finder.SharedPreferenceUtils
import com.metaldetector.golddetector.finder.model.TutorialModel
class IntroActivity : AbsBaseActivity<ActivityIntroBinding>(false) {
    lateinit var checkState : SharedPreferenceUtils
    private lateinit var dots: Array<ImageView>
    private var listFragment = 3
    var codeLang : String? =  null
    private var viewPagerAdapter: ViewPagerAdapter? = null
    var data = arrayListOf<TutorialModel>()
    override fun getFragmentID(): Int = 0
    override fun getLayoutId(): Int = R.layout.activity_intro

    override fun init() {
        initView()
        SystemUtils.setPreLanguage(this,codeLang)
        data = arrayListOf(
            TutorialModel(
                R.drawable.imv_intro1,
            ),
            TutorialModel(
                R.drawable.imv_intro2,
            ),
            TutorialModel(
                R.drawable.imv_intro3,
            )
        )
        viewPagerAdapter = ViewPagerAdapter()
        viewPagerAdapter!!.getData(data)
        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(100))
        compositePageTransformer.addTransformer { page: View, position: Float ->
            val r = 1 - Math.abs(position)
            page.scaleY = 0.8f + r * 0.2f
            val absPosition = Math.abs(position)
            page.alpha = 1.0f - (1.0f - 0.3f) * absPosition
        }
        binding.viewPager2.setPageTransformer(compositePageTransformer)
        bindViewModel()
        binding.viewPager2.setCurrentItem(1)
        binding.viewPager2.setCurrentItem(0)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onResume() {
        super.onResume()
        showSystemUI(true)
    }

    private fun initView() {
//        binding.tvNext.applyGradient_1(this)
        val paint = binding.tvNext.paint
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.strokeWidth = 1.6f

    }

    fun Int.dpToPx(context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    private fun bindViewModel() {
        binding.viewPager2.adapter = viewPagerAdapter
        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                addBottomDots(position)

            }
        })
        binding.imvClick.onSingleClick {
            val currentItem = binding.viewPager2.currentItem
            val itemCount = viewPagerAdapter?.itemCount ?:0
            if(currentItem < itemCount -1){
                binding.viewPager2.currentItem = currentItem + 1
            }else{
                val sharedPref = getSharedPreferences("MyPref", Context.MODE_PRIVATE)
                checkState = SharedPreferenceUtils.getInstance(this@IntroActivity)
                val state = checkState.getStringValue("firstPick")
                if (state.equals("false")){
                    startActivity(Intent(this@IntroActivity, MainActivity::class.java))
                }else{
                    startActivity(Intent(this@IntroActivity,PermissionActivity::class.java))
//                    if( SharedPreferenceUtils.getInstance(this@IntroActivity).getBooleanValue(
//                            Const.Interact)
//                    ){
//                        startActivity(Intent(this@IntroActivity, HomeActivity::class.java))
//                    }else{
//                        startActivity(Intent(this@IntroActivity, PermissionActivity::class.java))
//                    }
                }
                finish()
            }
        }
    }
    private fun addBottomDots(position: Int) {
        binding.apply {
            lnDots.removeAllViews()
            dots = Array(3) { ImageView(applicationContext) }
            for (i in 0..listFragment - 1) {
                dots[i] = ImageView(applicationContext)
                if (i == position) {
                    dots[i]
                        .setImageDrawable(resources.getDrawable(R.drawable.dot_yes))
                    val params = LinearLayout.LayoutParams(
                        dpToPx(20,applicationContext).toInt(),
                        dpToPx(8,applicationContext).toInt()
                    )
                    params.setMargins(8, 0, 8, 0)
                    lnDots.addView(dots[i], params)
                } else {
                    dots[i]
                        .setImageDrawable(
                            resources.getDrawable(R.drawable.dot_no)
                        )
                    val params = LinearLayout.LayoutParams(
                        dpToPx(8,applicationContext).toInt(),
                        dpToPx(8,applicationContext).toInt()
                    )
                    params.setMargins(8, 0, 8, 0)
                    lnDots.addView(dots[i], params)
                }
            }
        }
    }
}