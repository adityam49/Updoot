package com.ducktapedapps.updoot.ui

import androidx.lifecycle.MutableLiveData

interface InfiniteScrollVM {
    fun loadNextPage()
    val after: String?
    val isLoading: MutableLiveData<Boolean>
}