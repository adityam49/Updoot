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
import kotlinx.coroutines.launch
import javax.inject.Inject


@ExperimentalCoroutinesApi
class SubmissionsVM constructor(
        private val submissionRepo: SubmissionRepo
) : ViewModel(), InfiniteScrollVM {
    init {
        viewModelScope.launch {
            submissionRepo.loadAndSaveSubredditInfo()
        }
        loadPage()
    }

    override val isLoading = submissionRepo.isLoading

    var lastScrollPosition: Int = 0
    val postViewType = submissionRepo.postViewType
    val allSubmissions = submissionRepo.allSubmissions
    val toastMessage: StateFlow<SingleLiveEvent<String?>> = submissionRepo.toastMessage
    val subredditInfo = submissionRepo.subredditInfo

    override fun loadPage() {
        viewModelScope.launch { submissionRepo.loadPage() }
    }

    override fun hasNextPage() = submissionRepo.hasNextPage()

    fun reload() {
        submissionRepo.clearSubmissions()
        loadPage()
    }

    fun setPostViewType(type: SubmissionUiType) {
        viewModelScope.launch { submissionRepo.setPostViewType(type) }
    }

    fun changeSort(newSubredditSorting: SubredditSorting) {
        viewModelScope.launch { submissionRepo.changeSort(newSubredditSorting) }
    }

    fun upVote(name: String) {
        viewModelScope.launch { submissionRepo.vote(name, 1) }
    }

    fun downVote(name: String) {
        viewModelScope.launch { submissionRepo.vote(name, -1) }
    }

    fun save(id: String) {
        viewModelScope.launch { submissionRepo.save(id) }
    }
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
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SubmissionsVM(SubmissionRepo(redditClient, prefsDAO, submissionsCacheDAO, subreddit, subredditDAO)) as T
}