package com.kan.dev.st_042_video_to_mp3.ui
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart

class CustomCutterSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var animator: ValueAnimator? = null
    var isState = false
    private var minValue = 0
    private var maxValue = 100
    private var selectedMinValue = 0f
    private var selectedMaxValue = 0f
    private val thumbWidth = 5f // Chiều dài thumb
    private val thumbHeight = 20f
    private val thumbPadding = 60f// Chiều cao thumb

    private val paint: Paint = Paint().apply {
        color = Color.LTGRAY // Màu thanh cơ bản
        strokeWidth = 8f
    }

    private var whiteBarPosition = 0f // Vị trí hiện tại của thanh trắng
    private var isAnimating = false // Trạng thái của hoạt ảnh

    private val whiteBarPaint: Paint = Paint().apply {
        color = Color.WHITE // Màu trắng cho thanh di chuyển
    }

    private val thumbPaint: Paint = Paint().apply {
        color = Color.parseColor("#ED86D9") // Màu của thumb
    }

    private val pinkBackgroundPaint: Paint = Paint().apply {
        color = Color.argb(102, 237, 134, 217) // Màu hồng với độ trong suốt 40%
    }

    private val backgroundPaint: Paint = Paint().apply {
        color = Color.argb(120, 0, 0, 0) // Màu đen với opacity 30%
    }

    private val darkBackgroundPaint: Paint = Paint().apply {
        color = Color.TRANSPARENT // Màu nền tối
    }

    private val borderPaint: Paint = Paint().apply {
        color = Color.parseColor("#ED86D9") // Màu viền hồng
        strokeWidth = 12f
        style = Paint.Style.STROKE // Vẽ viền
    }

    private var isDraggingStartThumb = false
    private var isDraggingEndThumb = false
    private var isDarkInsideThumbs = true // Cờ điều khiển màu nền giữa hai thumb

    // Phương thức đổi màu nền
    fun changeBackgroundColor() {
        isDarkInsideThumbs = !isDarkInsideThumbs // Đổi trạng thái của cờ
        invalidate() // Vẽ lại view để cập nhật
    }

    fun resetBackgroundColorToDefault() {
        isDarkInsideThumbs = true // Đặt cờ lại về nền trong sáng, ngoài tối
        invalidate() // Vẽ lại view
    }

    init {
        setupDefaultValues()
    }
    private fun setupDefaultValues() {
        selectedMinValue = (minValue + (maxValue - minValue) / 3).toFloat() // Vị trí 1/3
        selectedMaxValue = (minValue + 2 * (maxValue - minValue) / 3).toFloat() // Vị trí 2/3
    }


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()

        canvas.drawRect(0f, 0f, width, height, darkBackgroundPaint)

        val minX = (width * (selectedMinValue - minValue) / (maxValue - minValue)) - thumbWidth / 2
        val maxX = (width * (selectedMaxValue - minValue) / (maxValue - minValue)) - thumbWidth / 2

        if (isAnimating) {
            val whiteX = (width * (whiteBarPosition - minValue) / (maxValue - minValue)) - thumbWidth / 2
            canvas.drawRect(
                whiteX, 0f,
                whiteX + thumbWidth, height,
                whiteBarPaint
            )
        }

        if (isDarkInsideThumbs) {
            // Vẽ nền tối bên ngoài hai thumb và nền sáng giữa hai thumb
            val leftBackgroundRect = RectF(0f, 0f, (minX + thumbWidth / 2).toFloat(), height)
            val rightBackgroundRect = RectF(maxX + thumbWidth / 2, 0f, width, height)
            canvas.drawRect(leftBackgroundRect, backgroundPaint)
            canvas.drawRect(rightBackgroundRect, backgroundPaint)
        } else {
            // Vẽ nền sáng bên ngoài hai thumb và nền tối giữa hai thumb
            val darkBackgroundRect = RectF(
                (minX + thumbWidth / 2).toFloat(),
                0f,
                (maxX + thumbWidth / 2).toFloat(),
                height
            )
            canvas.drawRect(darkBackgroundRect, backgroundPaint)
        }
        canvas.drawRect(
            minX, 0f, // Điểm bắt đầu từ trên cùng
            (minX + thumbWidth), height, // Điểm kết thúc ở dưới cùng
            thumbPaint
        )
        canvas.drawRect(
            maxX, 0f, // Điểm bắt đầu từ trên cùng
            maxX + thumbWidth, height, // Điểm kết thúc ở dưới cùng
            thumbPaint
        )
    }

    interface OnRangeSeekBarChangeListener {
        fun onRangeSeekBarValuesChanged(minValue: Float, maxValue: Float)
    }

    private var listener: OnRangeSeekBarChangeListener? = null

    fun setOnRangeSeekBarChangeListener(listener: OnRangeSeekBarChangeListener) {
        this.listener = listener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val width = width.toFloat()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val minX = (width * (selectedMinValue - minValue) / (maxValue - minValue))
                val maxX = (width * (selectedMaxValue - minValue) / (maxValue - minValue))

                if (x in (minX - thumbPadding)..(minX + thumbWidth + thumbPadding) && !isDraggingEndThumb) {
                    isDraggingStartThumb = true
                } else if (x in (maxX - thumbPadding)..(maxX + thumbWidth + thumbPadding) && !isDraggingStartThumb) {
                    isDraggingEndThumb = true
                }
            }

            MotionEvent.ACTION_MOVE -> {
                val progress = (x / width * (maxValue - minValue)).toInt() + minValue

                // Cập nhật giá trị min hoặc max dựa trên vị trí x
                if (isDraggingStartThumb) {
                    selectedMinValue =
                        progress.coerceAtMost((selectedMaxValue - 1).toInt()).toDouble().toFloat() // Đảm bảo không vượt quá selectedMaxValue
                } else if (isDraggingEndThumb) {
                    selectedMaxValue =
                        progress.coerceAtLeast((selectedMinValue + 1).toInt()).toFloat() // Đảm bảo không nhỏ hơn selectedMinValue
                }

                // Đảm bảo rằng selectedMinValue và selectedMaxValue không vượt quá giới hạn
                selectedMinValue = selectedMinValue.coerceIn(
                    minValue.toDouble().toFloat(),
                    selectedMaxValue.toDouble().toFloat()
                )
                selectedMaxValue = selectedMaxValue.coerceIn(selectedMinValue.toInt().toFloat(),
                    maxValue.toFloat()
                )
                listener?.onRangeSeekBarValuesChanged(selectedMinValue, selectedMaxValue)
                invalidate() // Vẽ lại view
            }

            MotionEvent.ACTION_UP -> {
                // Kết thúc sự kiện kéo thumb
                isDraggingStartThumb = false
                isDraggingEndThumb = false
            }
        }
        return true
    }

    fun getSelectedMinValue() = selectedMinValue
    fun getIsState() = isState
    fun getSelectedMaxValue() = selectedMaxValue
    fun getMaxValue() = maxValue
    fun setSelectedMinValue(newMinValue : Float) {
        selectedMinValue = newMinValue
        invalidate()
    }

    fun setSelectedMaxValue(newMaxValue : Float) {
        selectedMaxValue = newMaxValue
        invalidate()
    }
    fun setMaxValue(newMaxValue: Int) {
        maxValue = newMaxValue
        setupDefaultValues()
        invalidate() // Vẽ lại view
    }
}

//
//
//fun startWhiteBarAnimation() {
//    if (isAnimating) return // Để tránh chạy nhiều lần cùng lúc
//    val totalDistance = selectedMaxValue - selectedMinValue
//    val totalDuration = (totalDistance * 1000).toLong()
//    whiteBarPosition = selectedMinValue
//    // Tạo ValueAnimator để di chuyển thanh từ vị trí thumbStart đến thumbEnd
//    animator = ValueAnimator.ofFloat(selectedMinValue, selectedMaxValue)
//    animator!!.duration = totalDuration // Thời gian chạy là 1 giây
//    animator!!.addUpdateListener { animation ->
//        whiteBarPosition = animation.animatedValue as Float
//        invalidate() // Vẽ lại view mỗi khi giá trị thay đổi
//    }
//    animator!!.doOnStart {
//        isAnimating = true // Đánh dấu bắt đầu hoạt ảnh
//    }
//    animator!!.doOnEnd {
//        isAnimating = false // Hoạt ảnh kết thúc
//    }
//    animator!!.start()
//}


//fun pauseWhiteBarAnimation() {
//    animator?.pause()
//    isState = true
//}
//
//fun resumeWhiteBarAnimation() {
//    animator?.resume() // Tiếp tục hoạt ảnh nếu đã tạm dừng
//}
//
//fun resetWhiteBarPosition() {
//    // Đặt lại vị trí của thanh trắng về giá trị mặc định
//    whiteBarPosition = selectedMinValue
//    invalidate() // Vẽ lại view để cập nhật
//}