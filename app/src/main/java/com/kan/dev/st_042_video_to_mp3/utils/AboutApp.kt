package com.kan.dev.st_042_video_to_mp3.utils

import android.app.Activity
import android.content.Context
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Build
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.TextView
import androidx.core.view.WindowCompat


fun Activity.showSystemUI(white: Boolean) {
    if (white) {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    } else {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
    } else {
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
    }

}

var lastClickTime = 0L
fun View.onSingleClick(action: () -> Unit){
    this.setOnClickListener {
        if (System.currentTimeMillis() - lastClickTime >= 500) {
            action()
            lastClickTime = System.currentTimeMillis()
        }
    }
}

fun dpToPx(dp: Int, context: Context): Float {
    val density = context.resources.displayMetrics.density
    return (dp * density)
}



fun TextView.applyGradient(context: Context, colors: IntArray) {
    this.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {

            val paint = this@applyGradient.paint
            val textHeight = this@applyGradient.height.toFloat()  // Lấy chiều cao của TextView

            if (textHeight > 0) {
                // Gradient theo chiều dọc từ dưới lên trên
                val shader = LinearGradient(
                    0f, textHeight, 0f, 0f,  // Thay đổi từ (0f, 0f, textWidth, textSize) thành chiều dọc
                    colors,
                    null, Shader.TileMode.CLAMP
                )
                paint.shader = shader
                // Xóa listener để tránh việc gọi lại nhiều lần
                this@applyGradient.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }
    })
}

fun TextView.applyGradientWidth(context: Context, colors: IntArray) {
    this.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {

            val paint = this@applyGradientWidth.paint
            val textWidth = this@applyGradientWidth.width.toFloat()  // Lấy chiều rộng của TextView

            if (textWidth > 0) {
                // Gradient theo chiều ngang từ trái sang phải
                val shader = LinearGradient(
                    0f, 0f, textWidth, 0f,  // Thay đổi từ chiều dọc sang chiều ngang
                    colors,
                    null, Shader.TileMode.CLAMP
                )
                paint.shader = shader
                // Xóa listener để tránh việc gọi lại nhiều lần
                this@applyGradientWidth.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }
    })
}
