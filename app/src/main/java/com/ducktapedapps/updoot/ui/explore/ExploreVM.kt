package com.ducktapedapps.updoot.ui.explore

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class ExploreVM(private val exploreRepo: ExploreRepo, coroutineScope: CoroutineScope) {
    val isLoading = exploreRepo.isLoading
    val trendingSubs = exploreRepo.trendingSubs

    init {
        coroutineScope.launch {
            exploreRepo.loadTrendingSubs()
        }
    }
}