package com.example.ieltssynonymmatch

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class ProgressGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var scores: List<Int> = emptyList()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        paint.color = Color.parseColor("#1A237E")
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND

        pointPaint.style = Paint.Style.FILL
        pointPaint.color = Color.parseColor("#FF6F00")
    }

    fun setData(newScores: List<Int>) {
        this.scores = newScores
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (scores.size < 2) return

        val padding = 40f
        val w = width.toFloat() - (2 * padding)
        val h = height.toFloat() - (2 * padding)

        val maxScore = scores.maxOrNull()?.toFloat()?.coerceAtLeast(100f) ?: 100f
        val xStep = w / (scores.size - 1)

        path.reset()
        for (i in scores.indices) {
            val x = padding + (i * xStep)
            val y = (padding + h) - (scores[i].toFloat() / maxScore * h)

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        canvas.drawPath(path, paint)

        // Noktaları çiz
        for (i in scores.indices) {
            val x = padding + (i * xStep)
            val y = (padding + h) - (scores[i].toFloat() / maxScore * h)
            canvas.drawCircle(x, y, 8f, pointPaint)
        }
    }
}