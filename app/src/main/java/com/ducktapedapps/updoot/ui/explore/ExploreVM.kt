package com.ducktapedapps.updoot.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

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

@ExperimentalCoroutinesApi
class ExploreVMFactory @Inject constructor(private val exploreRepo: ExploreRepo) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = ExploreVM(exploreRepo) as T
}