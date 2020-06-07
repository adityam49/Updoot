package com.ducktapedapps.updoot.ui.subreddit

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.api.local.SubredditPrefsDAO
import com.ducktapedapps.updoot.api.local.submissionsCache.SubmissionsCacheDAO
import com.ducktapedapps.updoot.ui.InfiniteScrollVM
import com.ducktapedapps.updoot.utils.SingleLiveEvent
import com.ducktapedapps.updoot.utils.SubmissionUiType
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient


class SubmissionsVM constructor(
        redditClient: RedditClient,
        prefsDAO: SubredditPrefsDAO,
        submissionsCacheDAO: SubmissionsCacheDAO,
        subredditName: String
) : ViewModel(), InfiniteScrollVM {
    private val submissionRepo: SubmissionRepo = SubmissionRepo(redditClient, prefsDAO, submissionsCacheDAO, subredditName, viewModelScope)

    init {
        loadPage()
    }

    private val TAG = "SubmissionsVM"
    override val isLoading = submissionRepo.isLoading

    val postViewType: LiveData<SubmissionUiType> = submissionRepo.postViewType
    val allSubmissions = submissionRepo.allSubmissions
    val toastMessage: LiveData<SingleLiveEvent<String?>> = submissionRepo.toastMessage

    override fun loadPage() = submissionRepo.loadPage()

    override fun hasNextPage() = submissionRepo.hasNextPage()

    fun reload() {
        submissionRepo.clearSubmissions()
        loadPage()
    }

    fun toggleUi() = submissionRepo.togglePostViewType()

    fun changeSort(newSubredditSorting: SubredditSorting) = submissionRepo.changeSort(newSubredditSorting)

}