package com.ducktapedapps.updoot.utils

data class PagingModel<out T : Any>(
    val content: T,
    val footer: Footer
) {
    sealed class Footer {
        object Loading : Footer()
        data class UnLoadedPage(val pageKey: String?) : Footer()
        data class Error(val exception: Exception, val pageKey: String?) : Footer()
        object End : Footer()
    }
}