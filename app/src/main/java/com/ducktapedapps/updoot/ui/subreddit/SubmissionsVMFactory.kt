package com.ducktapedapps.updoot.ui.subreddit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ducktapedapps.updoot.api.local.SubmissionsCacheDAO
import com.ducktapedapps.updoot.api.local.SubredditPrefsDAO
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import javax.inject.Inject

class SubmissionsVMFactory @Inject constructor(
        private val redditClient: RedditClient,
        private val prefsDAO: SubredditPrefsDAO,
        private val submissionsCacheDAO: SubmissionsCacheDAO
) : ViewModelProvider.Factory {

    private lateinit var subreddit: String
    fun setSubreddit(name: String) {
        subreddit = name
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubmissionsVM::class.java)) {
            return SubmissionsVM(redditClient, prefsDAO, submissionsCacheDAO, subreddit) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
