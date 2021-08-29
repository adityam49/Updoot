package com.ducktapedapps.updoot.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ExploreVM @Inject constructor(
    getTrendingSubredditsUseCase: GetTrendingSubredditsUseCase,
) : ViewModel() {
    val trendingSubs = getTrendingSubredditsUseCase
        .trendingSubreddits
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
}