package com.ducktapedapps.updoot.viewModels

import androidx.lifecycle.MutableLiveData

interface InfiniteScrollVM {
    fun loadNextPage()
    val after: String?
    val isLoading: MutableLiveData<Boolean>
}