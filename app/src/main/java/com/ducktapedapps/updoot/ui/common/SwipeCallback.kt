package com.ducktapedapps.updoot.ui.common

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.RecyclerView
import com.ducktapedapps.updoot.ui.common.SwipeCallback.Swipe.*
import kotlin.math.abs

class SwipeCallback(
        @ColorInt private val extremeLeftColor: Int,
        @ColorInt private val leftColor: Int,
        @ColorInt private val rightColor: Int,
        @ColorInt private val extremeRightColor: Int,
        @ColorInt private val neutralColor: Int,
        private val extremeLeftDrawable: Drawable,
        private val leftDrawable: Drawable,
        private val rightDrawable: Drawable,
        private val extremeRightDrawable: Drawable,
        private val callback: Callback
) : ItemTouchHelper.SimpleCallback(0, LEFT or RIGHT) {

    interface Callback {
        fun extremeLeftAction(position: Int)
        fun leftAction(position: Int)
        fun rightAction(position: Int)
        fun extremeRightAction(position: Int)
    }

    private enum class Swipe {
        ACTION_EXTREME_LEFT,
        ACTION_LEFT,
        ACTION_RIGHT,
        ACTION_EXTREME_RIGHT
    }

    private var currentAction: Swipe? = null
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private companion object {
        const val EXTREME_THRESHOLD = 0.6
        const val THRESHOLD = 0.3
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false


    //never dismiss -> Float.MAX_VALUE
    override fun getSwipeEscapeVelocity(defaultValue: Float) = Float.MAX_VALUE
    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder) = Float.MAX_VALUE
    override fun getSwipeVelocityThreshold(defaultValue: Float) = Float.MAX_VALUE

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        //displace viewHolder
        viewHolder.itemView.translationX = dX

        drawBackgroundColor(c, viewHolder, dX)

        Log.d("Swipe", "onChildDraw: ${dX / viewHolder.itemView.width}")

        when (dX / viewHolder.itemView.width) {
            in -Double.MAX_VALUE..-EXTREME_THRESHOLD -> {
                viewHolder.itemView.run { drawExtremeRightDrawable(left, top, right, bottom, c, dX) }
            }
            in -EXTREME_THRESHOLD..-THRESHOLD -> {
                viewHolder.itemView.run { drawRightDrawable(left, top, right, bottom, c, dX) }
            }
            in THRESHOLD..EXTREME_THRESHOLD -> {
                viewHolder.itemView.run { drawLeftDrawable(left, top, right, bottom, c, dX) }
            }
            in EXTREME_THRESHOLD..Double.MAX_VALUE -> {
                viewHolder.itemView.run { drawExtremeLeftDrawable(left, top, right, bottom, c, dX) }
            }
        }
        //save action performed
        if (isCurrentlyActive) //is user actively manipulating swipe
            currentAction = when (dX / viewHolder.itemView.width) {
                in -Double.MAX_VALUE..-EXTREME_THRESHOLD -> ACTION_EXTREME_RIGHT
                in -EXTREME_THRESHOLD..-THRESHOLD -> ACTION_RIGHT
                in THRESHOLD..EXTREME_THRESHOLD -> ACTION_LEFT
                in EXTREME_THRESHOLD..Double.MAX_VALUE -> ACTION_EXTREME_LEFT
                else -> null
            }
    }

    private fun drawBackgroundColor(canvas: Canvas, viewHolder: RecyclerView.ViewHolder, dX: Float) {
        canvas.apply {
            if (dX < 0) {
                drawRect(
                        viewHolder.itemView.right.toFloat() + dX,
                        viewHolder.itemView.top.toFloat(),
                        viewHolder.itemView.right.toFloat(),
                        viewHolder.itemView.bottom.toFloat(),
                        paint.apply {
                            color = when (abs(dX / viewHolder.itemView.width)) {
                                in 0.0..THRESHOLD -> neutralColor
                                in THRESHOLD..EXTREME_THRESHOLD -> rightColor
                                else -> extremeRightColor
                            }
                        }
                )
            } else {
                canvas.drawRect(
                        0f,
                        viewHolder.itemView.top.toFloat(),
                        dX,
                        viewHolder.itemView.bottom.toFloat(),
                        paint.apply {
                            color = when (dX / viewHolder.itemView.width) {
                                in 0.0..THRESHOLD -> neutralColor
                                in THRESHOLD..EXTREME_THRESHOLD -> leftColor
                                else -> extremeLeftColor
                            }
                        }
                )
            }
        }
    }

    //TODO : draw drawable according to location of viewHolder dX
    private fun drawLeftDrawable(left: Int, top: Int, right: Int, bottom: Int, canvas: Canvas, dX: Float) {}

    private fun drawExtremeLeftDrawable(left: Int, top: Int, right: Int, bottom: Int, canvas: Canvas, dX: Float) {}

    private fun drawRightDrawable(left: Int, top: Int, right: Int, bottom: Int, canvas: Canvas, dX: Float) {}

    private fun drawExtremeRightDrawable(left: Int, top: Int, right: Int, bottom: Int, canvas: Canvas, dX: Float) {}


    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        when (currentAction) {
            ACTION_LEFT -> callback.leftAction(viewHolder.adapterPosition)
            ACTION_EXTREME_LEFT -> callback.extremeLeftAction(viewHolder.adapterPosition)
            ACTION_RIGHT -> callback.rightAction(viewHolder.adapterPosition)
            ACTION_EXTREME_RIGHT -> callback.extremeRightAction(viewHolder.adapterPosition)
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit
}