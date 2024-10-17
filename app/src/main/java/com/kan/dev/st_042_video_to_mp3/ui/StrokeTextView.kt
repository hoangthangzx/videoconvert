package com.kan.dev.st_042_video_to_mp3.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

//class StrokeTextView @JvmOverloads constructor(
//    context: Context,
//    attrs: AttributeSet? = null,
//    defStyle: Int = 0
//) : AppCompatTextView(context, attrs, defStyle) {
//
//    private var strokeColor: Int = Color.WHITE // Màu viền
//    private var strokeWidth: Float = 5f // Độ dày của viền
//
//    override fun onDraw(canvas: Canvas) {
//        val textColor = currentTextColor
//
//        // Vẽ viền ngoài (màu trắng)
//        paint.style = Paint.Style.STROKE
//        paint.strokeWidth = strokeWidth
//        setTextColor(strokeColor)
//        super.onDraw(canvas)
//
//        // Vẽ lại chữ bên trong (màu bên trong)
//        paint.style = Paint.Style.FILL
//        setTextColor(textColor)
//        super.onDraw(canvas)
//    }
//}

class StrokeTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {

    private var strokeColor: Int = Color.WHITE // Màu viền
    private var strokeWidth: Float = 5f // Độ dày của viền

    init {
        // Thiết lập padding tự động dựa trên kích thước văn bản
        setPadding(strokeWidth.toInt() + 20, strokeWidth.toInt() + 8, strokeWidth.toInt() + 16, strokeWidth.toInt() + 8)
    }

    override fun onDraw(canvas: Canvas) {
        val textColor = currentTextColor

        // Vẽ viền ngoài (màu trắng)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.color = strokeColor

        // Tính toán vị trí vẽ để căn giữa
        val x = (width - paint.measureText(text.toString())) / 2 // Căn giữa theo chiều ngang
        val y = height / 2f - (paint.descent() + paint.ascent()) / 2

        // Vẽ viền
        canvas.drawText(text.toString(), x, y, paint)

        // Vẽ lại chữ bên trong (màu bên trong)
        paint.style = Paint.Style.FILL
        paint.color = textColor
        canvas.drawText(text.toString(), x, y, paint)
    }
}