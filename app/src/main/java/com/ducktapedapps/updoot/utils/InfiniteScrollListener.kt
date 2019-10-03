package com.ducktapedapps.updoot.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ducktapedapps.updoot.viewModels.InfiniteScrollVM

class InfiniteScrollListener(private val layoutManager: LinearLayoutManager, private val viewModel: InfiniteScrollVM) : RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        //check for scroll down
        if (dy > 0) {
            if (layoutManager.itemCount <= 10) return // condition for no more pages

            if (layoutManager.findLastVisibleItemPosition() == layoutManager.itemCount - 10) {
                if (viewModel.isLoading.value == false && viewModel.after != null) {
                    viewModel.loadNextPage()
                }
            }
        }
    }
}