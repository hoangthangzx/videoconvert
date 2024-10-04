package com.kan.dev.st_042_video_to_mp3.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class StrokeTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {

    private var strokeColor: Int = Color.WHITE // Màu viền
    private var strokeWidth: Float = 5f // Độ dày của viền

    override fun onDraw(canvas: Canvas) {
        val textColor = currentTextColor

        // Vẽ viền ngoài (màu trắng)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        setTextColor(strokeColor)
        super.onDraw(canvas)

        // Vẽ lại chữ bên trong (màu bên trong)
        paint.style = Paint.Style.FILL
        setTextColor(textColor)
        super.onDraw(canvas)
    }
}