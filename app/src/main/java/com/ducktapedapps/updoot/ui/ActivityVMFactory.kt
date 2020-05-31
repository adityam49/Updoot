package com.ducktapedapps.updoot.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import javax.inject.Inject

class ActivityVMFactory @Inject constructor(private val redditClient: RedditClient) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActivityVM::class.java)) {
            return ActivityVM(redditClient) as T
        } else throw IllegalArgumentException("Unknown ViewModel class")
    }
}