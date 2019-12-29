package com.ducktapedapps.updoot.ui

import androidx.lifecycle.LiveData

interface InfiniteScrollVM {
    fun loadPage(appendPage : Boolean)
    fun hasNextPage() : Boolean
    val isLoading: LiveData<Boolean>
}