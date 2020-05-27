package com.ducktapedapps.updoot.ui.navDrawer

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior

class BottomNavDrawerCallback : BottomSheetBehavior.BottomSheetCallback() {
    private val onSlideActionsList = mutableListOf<OnSlideAction>()
    private val onStateChangeActionsList = mutableListOf<OnStateChangeAction>()

    fun addOnSlideAction(action: OnSlideAction) = onSlideActionsList.add(action)
    fun addOnStateChangeAction(action: OnStateChangeAction) = onStateChangeActionsList.add(action)
    
    override fun onSlide(bottomSheet: View, slideOffset: Float) =
            onSlideActionsList.forEach { it.onSlide(slideOffset) }


    override fun onStateChanged(bottomSheet: View, newState: Int) =
            onStateChangeActionsList.forEach { it.onStateChange(newState) }
}