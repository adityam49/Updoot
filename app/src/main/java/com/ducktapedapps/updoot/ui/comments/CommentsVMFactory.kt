package com.ducktapedapps.updoot.ui.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ducktapedapps.updoot.UpdootApplication
import javax.inject.Inject

class CommentsVMFactory(application: UpdootApplication,
                        private val id: String,
                        private val subreddit_name: String) : ViewModelProvider.Factory {

    init {
        application.updootComponent.inject(this)
    }

    @Inject
    lateinit var commentsRepo: CommentsRepo

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommentsVM::class.java)) {
            return CommentsVM(commentsRepo, id, subreddit_name) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}