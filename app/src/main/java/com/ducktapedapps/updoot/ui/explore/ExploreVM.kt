package com.ducktapedapps.updoot.ui.explore

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class ExploreVM @ViewModelInject constructor(private val exploreRepo: ExploreRepo) : ViewModel() {

    val isLoading = exploreRepo.isLoading
    val trendingSubs = exploreRepo.trendingSubs.distinctUntilChanged()

    init {
        loadSubs()
    }

    fun loadSubs() {
        viewModelScope.launch {
            exploreRepo.loadTrendingSubs()
        }
    }
}
