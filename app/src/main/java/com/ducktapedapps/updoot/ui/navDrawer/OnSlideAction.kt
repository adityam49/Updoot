package com.ducktapedapps.updoot.ui.navDrawer

import android.view.View
import kotlin.math.abs

interface OnSlideAction {
    fun onSlide(slideOffset: Float)
}

class ScrimVisibilityAdjuster(private val view: View) : OnSlideAction {
    override fun onSlide(slideOffset: Float) {
        with(view) {
            alpha = 0f + (abs(slideOffset / (0f - 0.3f)) * abs(0f - 1f))
            visibility = if (alpha < 0.01) View.GONE else View.VISIBLE
        }
    }
}

