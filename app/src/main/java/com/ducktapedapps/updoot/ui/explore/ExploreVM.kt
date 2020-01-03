package com.ducktapedapps.updoot.ui.explore

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ExploreVM(application: Application) : AndroidViewModel(application) {
    private val exploreRepo: ExploreRepo = ExploreRepo(application)

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