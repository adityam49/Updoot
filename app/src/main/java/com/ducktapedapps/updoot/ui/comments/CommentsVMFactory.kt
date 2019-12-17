package com.ducktapedapps.updoot.ui.comments

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CommentsVMFactory(private val appContext: Application,
                        private val id: String,
                        private val subreddit_name: String) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommentsVM::class.java)) {
            return CommentsVM(appContext, id, subreddit_name) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}