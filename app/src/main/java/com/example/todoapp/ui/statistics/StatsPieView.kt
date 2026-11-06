package com.example.todoapp.ui.statistics

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class StatsPieView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var completed = 0
    private var pending = 0

    private val paintCompleted = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
    }
    private val paintPending = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F44336")
        style = Paint.Style.FILL
    }
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 36f
        textAlign = Paint.Align.CENTER
    }
    private val paintLabel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 28f
        textAlign = Paint.Align.LEFT
    }

    fun setData(completed: Int, pending: Int) {
        this.completed = completed
        this.pending = pending
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val total = completed + pending
        if (total == 0) {
            canvas.drawText("Không có dữ liệu", width / 2f, height / 2f, paintText.apply { color = Color.GRAY })
            return
        }

        val size = minOf(width, height) * 0.6f
        val left = (width - size) / 2f
        val top = (height - size) / 2f
        val rect = RectF(left, top, left + size, top + size)

        val completedAngle = 360f * completed / total
        canvas.drawArc(rect, -90f, completedAngle, true, paintCompleted)
        canvas.drawArc(rect, -90f + completedAngle, 360f - completedAngle, true, paintPending)

        // Legend
        val legendX = 30f
        val legendY = height - 80f
        canvas.drawRect(legendX, legendY - 20f, legendX + 30f, legendY + 10f, paintCompleted)
        canvas.drawText("Hoàn thành: $completed", legendX + 40f, legendY, paintLabel)
        canvas.drawRect(legendX + 250f, legendY - 20f, legendX + 280f, legendY + 10f, paintPending)
        canvas.drawText("Chưa xong: $pending", legendX + 290f, legendY, paintLabel)
    }
}
