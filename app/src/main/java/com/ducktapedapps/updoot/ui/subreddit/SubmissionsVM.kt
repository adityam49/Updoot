package com.ducktapedapps.updoot.ui.subreddit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.api.local.SubmissionsCacheDAO
import com.ducktapedapps.updoot.api.local.SubredditDAO
import com.ducktapedapps.updoot.api.local.SubredditPrefsDAO
import com.ducktapedapps.updoot.ui.common.InfiniteScrollVM
import com.ducktapedapps.updoot.utils.SingleLiveEvent
import com.ducktapedapps.updoot.utils.SubmissionUiType
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


@ExperimentalCoroutinesApi
class SubmissionsVM constructor(
        redditClient: RedditClient,
        prefsDAO: SubredditPrefsDAO,
        submissionsCacheDAO: SubmissionsCacheDAO,
        subredditName: String,
        subredditDAO: SubredditDAO
) : ViewModel(), InfiniteScrollVM {
    private val submissionRepo =
            SubmissionRepo(redditClient, prefsDAO, submissionsCacheDAO, subredditName, viewModelScope, subredditDAO)

    init {
        loadPage()
    }

    private val TAG = "SubmissionsVM"
    override val isLoading = submissionRepo.isLoading

    var lastScrollPosition: Int = 0
    val postViewType = submissionRepo.postViewType
    val allSubmissions = submissionRepo.allSubmissions
    val toastMessage: StateFlow<SingleLiveEvent<String?>> = submissionRepo.toastMessage
    val subredditInfo = submissionRepo.subredditInfo

    override fun loadPage() = submissionRepo.loadPage()

    override fun hasNextPage() = submissionRepo.hasNextPage()

    fun reload() {
        submissionRepo.clearSubmissions()
        loadPage()
    }

    fun setPostViewType(type: SubmissionUiType) = submissionRepo.setPostViewType(type)

    fun changeSort(newSubredditSorting: SubredditSorting) = submissionRepo.changeSort(newSubredditSorting)

    fun upVote(name: String) = submissionRepo.vote(name, 1)

    fun downVote(name: String) = submissionRepo.vote(name, -1)

    fun save(id: String) = submissionRepo.save(id)
}

@ExperimentalCoroutinesApi
class SubmissionsVMFactory @Inject constructor(
        private val redditClient: RedditClient,
        private val prefsDAO: SubredditPrefsDAO,
        private val submissionsCacheDAO: SubmissionsCacheDAO,
        private val subredditDAO: SubredditDAO
) : ViewModelProvider.Factory {

    private lateinit var subreddit: String
    fun setSubreddit(name: String) {
        subreddit = name
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = SubmissionsVM(redditClient, prefsDAO, submissionsCacheDAO, subreddit, subredditDAO) as T
}