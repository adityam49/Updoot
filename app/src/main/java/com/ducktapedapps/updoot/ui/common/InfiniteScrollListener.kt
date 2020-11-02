package com.ducktapedapps.updoot.ui.common

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class InfiniteScrollListener(private val layoutManager: LinearLayoutManager, private val viewModel: InfiniteScrollVM) : RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        //check for scroll down
        if (dy > 0) {
            if (layoutManager.itemCount <= 10) return // condition for no more pages

            if (layoutManager.findLastVisibleItemPosition() == layoutManager.itemCount - 10) {
                if (!viewModel.isLoading.value && viewModel.hasNextPage()) {
                    viewModel.loadPage()
                }
            }
        }
    }
}