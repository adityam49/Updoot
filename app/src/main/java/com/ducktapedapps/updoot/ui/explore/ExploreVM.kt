package com.ducktapedapps.updoot.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class ExploreVM(private val exploreRepo: ExploreRepo) : ViewModel() {

    val isLoading = exploreRepo.isLoading
    val trendingSubs = exploreRepo.trendingSubs

    init {
        viewModelScope.launch {
            exploreRepo.loadTrendingSubs()
        }
    }
}