package com.kan.dev.st_042_video_to_mp3.ui
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class CustomRangeSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var minValue = 0
    private var maxValue = 100
    private var selectedMinValue = 0f
    private var selectedMaxValue = 0f

    private val thumbWidth = 20f // Chiều dài thumb
    private val thumbHeight = 20f
    private val thumbPadding = 60f// Chiều cao thumb

    private val paint: Paint = Paint().apply {
        color = Color.LTGRAY // Màu thanh cơ bản
        strokeWidth = 8f
    }

    private val thumbPaint: Paint = Paint().apply {
        color = Color.MAGENTA // Màu của thumb
    }

    private val backgroundPaint: Paint = Paint().apply {
        color = Color.argb(120, 0, 0, 0) // Màu đen với opacity 30%
    }

    private val darkBackgroundPaint: Paint = Paint().apply {
        color = Color.TRANSPARENT // Màu nền tối
    }

    private val borderPaint: Paint = Paint().apply {
        color = Color.MAGENTA // Màu viền hồng
        strokeWidth = 8f
        style = Paint.Style.STROKE // Vẽ viền
    }

    private var isDraggingStartThumb = false
    private var isDraggingEndThumb = false

    init {
        // Thiết lập giá trị mặc định cho selectedMinValue và selectedMaxValue
        setupDefaultValues()
    }

    private fun setupDefaultValues() {
        selectedMinValue = (minValue + (maxValue - minValue) / 3).toFloat() // Vị trí 1/3
        selectedMaxValue = (minValue + 2 * (maxValue - minValue) / 3).toFloat() // Vị trí 2/3
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()

        // Vẽ nền tối toàn bộ
        canvas.drawRect(0f, 0f, width, height, darkBackgroundPaint)

        // Tính toán vị trí của thumb
        val minX = (width * (selectedMinValue - minValue) / (maxValue - minValue)) - thumbWidth / 2
        val maxX = (width * (selectedMaxValue - minValue) / (maxValue - minValue)) - thumbWidth / 2

        // Vẽ nền sáng giữa hai thumb
        val backgroundRect = RectF((minX + thumbWidth / 2).toFloat(), 0f, maxX + thumbWidth / 2, height)
        canvas.drawRoundRect(backgroundRect, 20f, 20f, borderPaint) // Vẽ viền hồng


        // Vẽ màu nền đen bên ngoài hai thumb
        val leftBackgroundRect = RectF(0f, 0f, (minX + thumbWidth / 2).toFloat(), height)
        val rightBackgroundRect = RectF(maxX + thumbWidth / 2, 0f, width, height)
        canvas.drawRect(leftBackgroundRect, backgroundPaint)
        canvas.drawRect(rightBackgroundRect, backgroundPaint)



        canvas.drawRect(
            minX.toFloat(), (height / 2 - thumbHeight / 2),
            (minX + thumbWidth).toFloat(), (height / 2 + thumbHeight / 2),
            thumbPaint
        )

        canvas.drawRect(
            maxX, (height / 2 - thumbHeight / 2),
            maxX + thumbWidth, (height / 2 + thumbHeight / 2),
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
                val minX = (width * (selectedMinValue - minValue) / (maxValue - minValue)) - thumbWidth / 2
                val maxX = (width * (selectedMaxValue - minValue) / (maxValue - minValue)) - thumbWidth / 2

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
    fun getSelectedMaxValue() = selectedMaxValue
    fun setSelectedMinValue(newMinValue : Float) {
        selectedMinValue = newMinValue
        setupDefaultValues()
        invalidate()
    }

    fun setSelectedMaxValue(newMaxValue : Float) {
        selectedMinValue = newMaxValue
        setupDefaultValues()
        invalidate()
    }
    fun setMaxValue(newMaxValue: Int) {
        maxValue = newMaxValue
        setupDefaultValues()
        invalidate() // Vẽ lại view
    }
}