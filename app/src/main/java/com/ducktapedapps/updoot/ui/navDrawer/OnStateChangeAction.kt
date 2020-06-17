package com.ducktapedapps.updoot.ui.navDrawer

import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED

interface OnStateChangeAction {
    fun onStateChange(newState: Int)
}

class ToolbarMenuSwapper(
        private val toolbar: Toolbar,
        private val getCurrentDestinationMenu: () -> Int?
) : OnStateChangeAction {
    override fun onStateChange(newState: Int) {
        with(toolbar) {
            menu.clear()
            if (newState == STATE_COLLAPSED)
                getCurrentDestinationMenu()?.let { inflateMenu(it) }
            else
                inflateMenu(com.ducktapedapps.updoot.R.menu.navigation_drawer_menu)
        }
    }
}