package com.ducktapedapps.updoot.explore

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class ExploreVM @ViewModelInject constructor(
    getTrendingSubredditsUseCase: GetTrendingSubredditsUseCase,
) : ViewModel() {
    val trendingSubs = getTrendingSubredditsUseCase
        .trendingSubreddits
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
}