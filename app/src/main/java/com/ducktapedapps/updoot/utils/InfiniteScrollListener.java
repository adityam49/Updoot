package com.ducktapedapps.updoot.utils;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ducktapedapps.updoot.viewModels.InfiniteScrollVM;

import org.jetbrains.annotations.NotNull;

public class InfiniteScrollListener extends RecyclerView.OnScrollListener {
    private LinearLayoutManager layoutManager;
    private InfiniteScrollVM viewModel;

    public InfiniteScrollListener(LinearLayoutManager layoutManager, InfiniteScrollVM viewModel) {
        this.layoutManager = layoutManager;
        this.viewModel = viewModel;
    }

    @Override
    public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
        //check for scroll down
        if (dy > 0) {
            int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
            int totalItems = layoutManager.getItemCount();

            // 10 is next page prefetch threshold
            if (totalItems <= 10) return; //condition for no more pages

            if (lastVisiblePosition == totalItems - 10) {
                if ((viewModel.getIsLoading().getValue() != null && !viewModel.getIsLoading().getValue() && viewModel.getAfter() != null)) {
                    viewModel.loadNextPage();
                }
            }
        }
    }
}