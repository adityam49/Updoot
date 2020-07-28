package com.ducktapedapps.updoot.ui.common

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.ducktapedapps.updoot.ui.subreddit.SubmissionsVM

class ScrollPositionListener(
        private val linearLayoutManager: LinearLayoutManager,
        private val submissionsVM: SubmissionsVM
) : RecyclerView.OnScrollListener() {
    private var manualScrollingFlag = false
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        when (newState) {
            SCROLL_STATE_DRAGGING -> manualScrollingFlag = true
            SCROLL_STATE_IDLE -> {
                if (manualScrollingFlag) {
                    submissionsVM.lastScrollPosition = linearLayoutManager.findFirstVisibleItemPosition()
                    manualScrollingFlag = false
                }
            }
            else -> Unit
        }
    }
}