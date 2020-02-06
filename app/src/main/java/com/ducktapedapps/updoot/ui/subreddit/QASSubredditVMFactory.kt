package com.ducktapedapps.updoot.ui.subreddit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ducktapedapps.updoot.utils.accountManagement.Reddit
import javax.inject.Inject

class QASSubredditVMFactory @Inject constructor(val reddit: Reddit) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QASSubredditVM::class.java)) {
            return QASSubredditVM(reddit) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}