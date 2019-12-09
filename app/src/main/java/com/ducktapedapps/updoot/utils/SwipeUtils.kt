package com.ducktapedapps.updoot.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.ducktapedapps.updoot.R

class SwipeUtils(context: Context?, callback: SwipeActionCallback) : ItemTouchHelper.Callback() {
    private var swipeBack = false
    private val swipeActionCallback: SwipeActionCallback = callback
    private var performActionIndex = 0 // left = -2 ,slightLeft = -1 ,slightRight = 1 , right = 2
    private val paint: Paint = Paint()
    private var upVoteColor = 0
    private var saveContentColor = 0
    private var downVoteColor = 0
    private var optionsColor = 0
    private lateinit var upVoteIcon: Bitmap
    private lateinit var downVoteIcon: Bitmap
    private lateinit var saveIcon: Bitmap

    init {
        if (context != null) {
            upVoteIcon = getBitmap(context, R.drawable.ic_upvote_24dp)
            downVoteIcon = getBitmap(context, R.drawable.ic_downvote_24dp)
            saveIcon = getBitmap(context, R.drawable.ic_star_black_24dp)
            upVoteColor = ContextCompat.getColor(context, R.color.upVoteColor)
            downVoteColor = ContextCompat.getColor(context, R.color.downVoteColor)
            saveContentColor = ContextCompat.getColor(context, R.color.saveContentColor)
            optionsColor = ContextCompat.getColor(context, R.color.neutralColor)
        }
    }


    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) { // Get RecyclerView item from the ViewHolder
            val itemView = viewHolder.itemView
            val itemViewWidth = itemView.width.toFloat()
            val itemViewTop = itemView.top.toFloat()
            val itemViewBottom = itemView.bottom.toFloat()
            val itemViewLeft = itemView.left.toFloat()
            val itemViewRight = itemView.right.toFloat()
            val firstThreshold = itemViewWidth * 0.2f
            val secondThreshold = itemViewWidth * 0.6f
            if (dX > 0) { // right swipe
                paint.color = upVoteColor
                if (dX < firstThreshold) {
                    paint.alpha = (255 * (dX / firstThreshold)).toInt() //right swipe cancel threshold of 20%
                    c.drawRect(itemViewLeft, itemViewTop, dX, itemViewBottom, paint)
                } else if (dX > firstThreshold && dX <= secondThreshold) {
                    c.drawRect(itemViewLeft, itemViewTop, dX, itemViewBottom, paint)
                    c.drawBitmap(upVoteIcon,
                            itemViewLeft + itemViewWidth * 0.1f,
                            itemViewTop + (itemViewBottom - itemViewTop - upVoteIcon.height) / 2,
                            null)
                } else if (dX >= secondThreshold) {
                    paint.color = saveContentColor // swiped right more than 60%
                    c.drawRect(itemViewLeft, itemViewTop, dX, itemViewBottom, paint)
                    c.drawBitmap(saveIcon,
                            itemViewLeft + itemViewWidth * 0.1f,
                            itemViewTop + (itemViewBottom - itemViewTop - upVoteIcon.height) / 2,
                            null)
                }
            } else if (dX < 0) { // left swipe
                paint.color = downVoteColor
                if (dX > -firstThreshold) {
                    paint.alpha = (255 * (-dX / firstThreshold)).toInt() // left swipe cancel threshold of 20%
                    c.drawRect(itemViewRight + dX, itemViewTop, itemViewRight, itemViewBottom, paint)
                } else if (dX < -firstThreshold && dX > -secondThreshold) {
                    c.drawRect(itemViewRight + dX, itemViewTop, itemViewRight, itemViewBottom, paint)
                    c.drawBitmap(downVoteIcon,
                            itemViewRight - itemViewWidth * 0.1f - downVoteIcon.width,
                            itemViewTop + (itemViewBottom - itemViewTop - downVoteIcon.height) / 2,
                            null)
                } else if (dX <= -secondThreshold) {
                    paint.color = optionsColor // swiped left more than 60%
                    c.drawRect(itemViewRight + dX, itemViewTop, itemViewRight, itemViewBottom, paint)
                }
            } else { // when view is back to original position
                when (performActionIndex) {
                    -2 -> swipeActionCallback.performLeftSwipeAction(viewHolder.adapterPosition)
                    -1 -> swipeActionCallback.performSlightLeftSwipeAction(viewHolder.adapterPosition)
                    1 -> swipeActionCallback.performSlightRightSwipeAction(viewHolder.adapterPosition)
                    2 -> swipeActionCallback.performRightSwipeAction(viewHolder.adapterPosition)
                }
                if (performActionIndex != 0) performActionIndex = 0
            }
            setTouchListener(c, recyclerView, dX)
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchListener(c: Canvas, recyclerView: RecyclerView, dX: Float) {
        recyclerView.setOnTouchListener { _: View?, event: MotionEvent ->
            swipeBack = event.action == MotionEvent.ACTION_UP
            if (swipeBack) {
                val firstThreshold = c.width * 0.2f
                val secondThreshold = c.width * 0.6f
                performActionIndex = if (dX < 0) { // swipe left
                    if (dX > -firstThreshold) return@setOnTouchListener false // left swipe action cancel threshold is 20%
                    if (dX > -secondThreshold) -1                             // left swipe less than 60%
                    else -2                                                   // left swipe more than 50%
                } else { // swipe right
                    if (dX < firstThreshold) return@setOnTouchListener false // right swipe action cancel threshold is 20%
                    if (dX > firstThreshold && dX < secondThreshold) 1       // right swipe less than 60%
                    else 2                                                   // right swipe more than 60%
                }
            }
            false
        }
    }

    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        return if (swipeBack) {
            swipeBack = false
            0
        } else super.convertToAbsoluteDirection(flags, layoutDirection)

//        if (swipeBack) {
//            swipeBack = false
//            return 0
//        }
//        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
    interface SwipeActionCallback {
        fun performSlightLeftSwipeAction(adapterPosition: Int)
        fun performSlightRightSwipeAction(adapterPosition: Int)
        fun performLeftSwipeAction(adapterPosition: Int)
        fun performRightSwipeAction(adapterPosition: Int)
    }

    companion object {
        private fun getBitmap(context: Context, vectorDrawable: Int): Bitmap {
            val drawable = ContextCompat.getDrawable(context, vectorDrawable)
            val bitmap = Bitmap.createBitmap(
                    drawable?.intrinsicWidth ?: 0,
                    drawable?.intrinsicHeight ?: 0,
                    Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable?.setBounds(0, 0, canvas.width, canvas.height)
            drawable?.draw(canvas)
            return bitmap

        }
    }
}

