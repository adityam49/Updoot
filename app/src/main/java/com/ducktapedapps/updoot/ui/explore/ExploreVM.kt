package com.ducktapedapps.updoot.ui.explore

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ExploreVM(private val exploreRepo: ExploreRepo) : ViewModel() {

    val isLoading = exploreRepo.isLoading
    val result = exploreRepo.results

    fun searchSubreddit(query: String) {
        viewModelScope.launch {
            exploreRepo.searchSubreddit(query)
        }
    }

    override fun onCleared() {
        Log.i("ExploreVM", "onCleared")
        super.onCleared()
    }
}