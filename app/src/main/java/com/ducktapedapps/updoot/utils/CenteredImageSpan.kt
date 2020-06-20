package com.ducktapedapps.updoot.utils

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.ImageSpan

class CenteredImageSpan(private val d: Drawable) : ImageSpan(d) {
    override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        canvas.apply {
            save()
            translate(x, ((bottom - top) / 2 - d.bounds.height() / 2).toFloat())
            d.draw(this)
            restore()
        }
    }
}