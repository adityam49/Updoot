package com.ducktapedapps.updoot.ui.common

import kotlinx.coroutines.flow.StateFlow

interface InfiniteScrollVM {
    fun loadPage()
    fun hasNextPage(): Boolean
    val isLoading: StateFlow<Boolean>
}