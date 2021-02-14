package com.ducktapedapps.updoot.utils

import kotlinx.coroutines.flow.Flow

sealed class Page<out T : Any> {

    object LoadingPage : Page<Nothing>()

    data class LoadedPage<out T : Any>(val content: Flow<List<T>>, val nextPageKey: String?) :
        Page<T>() {
        fun hasNextPage(): Boolean = nextPageKey != null
    }

    data class ErrorPage(val currentPageKey: String?, val errorReason: String) : Page<Nothing>()

    object End : Page<Nothing>()

}