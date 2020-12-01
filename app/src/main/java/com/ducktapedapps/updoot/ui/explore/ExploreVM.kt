package com.ducktapedapps.updoot.ui.explore

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class ExploreVM constructor(private val exploreRepo: ExploreRepo, private val coroutineScope: CoroutineScope) {
    val isLoading = exploreRepo.isLoading
    val trendingSubs = exploreRepo.trendingSubs.distinctUntilChanged()

    init {
        loadSubs()
    }

    fun loadSubs() {
        coroutineScope.launch {
            exploreRepo.loadTrendingSubs()
        }
    }
}