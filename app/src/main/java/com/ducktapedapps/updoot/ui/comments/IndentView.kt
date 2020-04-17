package com.ducktapedapps.updoot.ui.comments

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.ducktapedapps.updoot.R
import kotlin.math.ceil
import kotlin.math.min

class IndentView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private lateinit var threadPaint: Paint
    private var indentLevel = 0
    private var threadWidth = 0f
    private var threadSeparationSpace = 0f
    private var singleThreadMode = false

    init {
        if (attrs != null) {
            val ta: TypedArray =
                    context.obtainStyledAttributes(
                            attrs,
                            R.styleable.IndentView
                    )
            singleThreadMode = ta.getBoolean(R.styleable.IndentView_singleThreadMode, false)
            threadSeparationSpace =
                    ta.getDimension(R.styleable.IndentView_threadSeparationSpace, 3f).toDP()
            threadWidth = ta.getDimension(R.styleable.IndentView_threadWidth, 1f).toDP()
            threadPaint = Paint().apply {
                color = ta.getColor(
                        R.styleable.IndentView_threadColor,
                        ContextCompat.getColor(context, R.color.color_secondary_variant)
                )
                strokeWidth = threadWidth
            }
            indentLevel = ta.getInt(R.styleable.IndentView_indentLevel, 0)
            ta.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val requestedWidth = MeasureSpec.getSize(widthMeasureSpec)
        val requestedWidthMode = MeasureSpec.getMode(widthMeasureSpec)
        val desiredWidth: Int = +paddingStart + paddingEnd +
                ceil((indentLevel * (threadSeparationSpace + threadWidth))).toInt()

        val _width = when (requestedWidthMode) {
            MeasureSpec.EXACTLY -> requestedWidth
            MeasureSpec.AT_MOST -> min(
                    requestedWidth,
                    desiredWidth
            ) //AT_MOST for wrap_content and match_parent
            else -> desiredWidth
        }
        setMeasuredDimension(_width, MeasureSpec.getSize(heightMeasureSpec))
    }

    override fun onDraw(canvas: Canvas) {
        if (indentLevel != 0) {
            if (singleThreadMode)
                canvas.drawLine(
                        paddingLeft + (indentLevel - 1) * (threadSeparationSpace) + (indentLevel + 1) * threadWidth,
                        top.toFloat(),
                        paddingLeft + (indentLevel - 1) * (threadSeparationSpace) + (indentLevel + 1) * threadWidth,
                        bottom.toFloat(),
                        threadPaint
                )
            else
                for (i in 1..indentLevel)
                    canvas.drawLine(
                            paddingLeft + (i - 1) * (threadSeparationSpace) + threadWidth * (i + 1),
                            top.toFloat(),
                            paddingLeft + (i - 1) * (threadSeparationSpace) + threadWidth * (i + 1),
                            bottom.toFloat(),
                            threadPaint
                    )

        }
    }

    fun setIndentLevel(newLevel: Int) {
        indentLevel = newLevel
        requestLayout()
    }

    private fun Float.toDP() = resources.displayMetrics.density * this
}
