package com.ducktapedapps.updoot.ui.common

interface SwipeableViewHolder {
    fun getExtremeLeftSwipeData(): String?
    fun getLeftSwipeData(): String?
    fun getRightSwipeData(): String?
    fun getExtremeRightSwipeData(): String?
}