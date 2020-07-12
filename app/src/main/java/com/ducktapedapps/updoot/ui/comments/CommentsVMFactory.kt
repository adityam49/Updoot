package com.ducktapedapps.updoot.ui.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ducktapedapps.updoot.api.local.SubmissionsCacheDAO
import javax.inject.Inject

class CommentsVMFactory @Inject constructor(
        private val commentsRepo: CommentsRepo,
        private val submissionsCacheDAO: SubmissionsCacheDAO
) : ViewModelProvider.Factory {
    private lateinit var subredditName: String
    private lateinit var id: String

    fun setSubredditAndId(name: String, id: String) {
        subredditName = name
        this.id = id
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommentsVM::class.java)) {
            return CommentsVM(commentsRepo, submissionsCacheDAO, id, subredditName) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}