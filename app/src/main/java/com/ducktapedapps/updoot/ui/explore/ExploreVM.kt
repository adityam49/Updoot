package com.ducktapedapps.updoot.ui.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ExploreVM(private val exploreRepo: ExploreRepo) : ViewModel() {

    private var currentSearchJob: Job? = null
    val isLoading = exploreRepo.isLoading
    val searchResults = exploreRepo.results
    val trendingSubs: LiveData<List<ExploreUiModel>> = exploreRepo.trendingSubs

    init {
        viewModelScope.launch {
            exploreRepo.loadTrendingSubs()
        }
    }

    fun searchSubreddit(query: String) {
        currentSearchJob?.cancel()
        currentSearchJob = viewModelScope.launch {
            exploreRepo.searchSubreddit(query)
        }
    }
}