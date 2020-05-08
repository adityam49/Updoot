package com.ducktapedapps.updoot.utils

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ReplacementSpan
import androidx.annotation.ColorInt
import kotlin.math.roundToInt

class RoundedBackgroundSpan(
        @ColorInt private val backgroundColor: Int,
        @ColorInt private val foregroundColor: Int
) : ReplacementSpan() {

    companion object {
        private const val cornerRadius = 8f
    }

    private var padding = 0f
    private var spanWidth = 0f

    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        val textWidth = paint.measureText(text, start, end)
        spanWidth = textWidth
        padding = textWidth / text.length
        return (spanWidth + 2 * padding).roundToInt()
    }


    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, baseLine: Int, bottom: Int, paint: Paint) {
        canvas.apply {
            drawRoundRect(x, top.toFloat(), spanWidth + padding * 2, bottom.toFloat(), cornerRadius, cornerRadius, paint.apply {
                flags = Paint.ANTI_ALIAS_FLAG
                color = backgroundColor
            })
            drawText(text.toString(), start, end, x + padding, baseLine.toFloat(), paint.apply { color = foregroundColor })
        }
    }
}