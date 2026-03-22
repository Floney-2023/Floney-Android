package com.aos.floney.view.analyze

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import timber.log.Timber

class AnalyzeOutComeBarChart(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF6200EE.toInt() // 예제 색상
        style = Paint.Style.FILL
    }

    private var data = arrayListOf<Float>(10f, 20f, 30f, 30f, 30f) // 차트 데이터
    private var total = data.sum() // 데이터 총합 계산
    private var colorArr = listOf<Int>()
    private var colorIdx = 0

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (total <= 0f || data.isEmpty()) return

        val visibleData = data.mapIndexed { index, value -> index to value }.filter { it.second > 0f }
        if (visibleData.isEmpty()) return

        val top = 0f
        val bottom = height.toFloat()
        val radius = (height / 2f).coerceAtLeast(1f)
        var currentLeft = 0f
        colorIdx = 0

        for ((drawIndex, item) in visibleData.withIndex()) {
            val index = item.first
            val value = item.second
            Timber.e("value $value")

            val segmentWidth = width * (value / total)
            if (segmentWidth <= 0f) continue

            val left = currentLeft
            val right = (left + segmentWidth).coerceAtMost(width.toFloat())
            currentLeft = right

            paint.color = resolveColor(index, value)

            val rect = RectF(left, top, right, bottom)
            when {
                visibleData.size == 1 -> {
                    canvas.drawRoundRect(rect, radius, radius, paint)
                }
                drawIndex == 0 -> {
                    val path = Path().apply {
                        addRoundRect(
                            rect,
                            floatArrayOf(radius, radius, 0f, 0f, 0f, 0f, radius, radius),
                            Path.Direction.CW
                        )
                    }
                    canvas.drawPath(path, paint)
                }
                drawIndex == visibleData.lastIndex -> {
                    val path = Path().apply {
                        addRoundRect(
                            rect,
                            floatArrayOf(0f, 0f, radius, radius, radius, radius, 0f, 0f),
                            Path.Direction.CW
                        )
                    }
                    canvas.drawPath(path, paint)
                }
                else -> canvas.drawRect(rect, paint)
            }
        }
    }

    private fun resolveColor(index: Int, value: Float): Int {
        return if (index == 0) {
            Color.parseColor("#FFDE31")
        } else if (index == 1 && value.toInt() == 0) {
            Color.parseColor("#FFFFFF")
        } else if (index == 1) {
            Color.parseColor("#FF965B")
        } else if (index == 2) {
            Color.parseColor("#E56E73")
        } else {
            if (colorArr.isEmpty()) {
                Color.TRANSPARENT
            } else {
                val safeColorIdx = colorIdx.coerceIn(0, colorArr.size - 1)
                colorIdx++
                colorArr[safeColorIdx]
            }
        }
    }

    fun setData(newData: List<Float>, colorTempArr: List<Int>) {
        data.clear()

        newData.forEachIndexed { index, fl ->
            if(newData.size -1 == index && fl < 1) {
                data.add(fl + 2)
            } else {
                data.add(fl)
            }
        }

        total = data.sum()
        colorArr = colorTempArr
        invalidate()
    }

    fun clearColorIdx() {
        colorIdx = 0
    }
}
