package com.ducktapedapps.updoot.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class CustomItemAnimator : DefaultItemAnimator() {
    override fun animateAdd(viewHolder: RecyclerView.ViewHolder): Boolean {
        if (viewHolder.layoutPosition == 0) viewHolder.itemView.translationY = viewHolder.itemView.height.toFloat()
        else viewHolder.itemView.translationY = viewHolder.itemView.top.toFloat()

        viewHolder.itemView.animate()
                .translationY(0f) //move to its original y co-ordinate
                .setInterpolator(DecelerateInterpolator(3.0f))
                .setDuration(700)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        dispatchAddFinished(viewHolder)
                    }
                })
                .start()
        return false
    }
}
