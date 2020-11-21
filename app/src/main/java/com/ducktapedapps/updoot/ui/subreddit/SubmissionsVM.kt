package com.ducktapedapps.updoot.ui.subreddit

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.ui.common.InfiniteScrollVM
import com.ducktapedapps.updoot.utils.SingleLiveEvent
import com.ducktapedapps.updoot.utils.SubmissionUiType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SubmissionsVM @ViewModelInject constructor(
        private val submissionRepo: SubmissionRepo,
        @Assisted savedStateHandle: SavedStateHandle
) : ViewModel(), InfiniteScrollVM {
    private val subreddit: String = savedStateHandle.get<String>(SubredditFragment.SUBREDDIT_KEY)!!

    init {
        viewModelScope.launch {
            submissionRepo.loadAndSaveSubredditInfo(subreddit)
        }
        loadPage()
    }

    override val isLoading = submissionRepo.isLoading

    var lastScrollPosition: Int = 0
    val postViewType = submissionRepo
            .postViewType(subreddit)
            .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.Lazily,
                    initialValue = SubmissionUiType.COMPACT
            )
    val allSubmissions = submissionRepo
            .allSubmissions
            .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.Lazily,
                    initialValue = emptyList(),
            )

    val toastMessage: StateFlow<SingleLiveEvent<String?>> = submissionRepo.toastMessage
    val subredditInfo = submissionRepo
            .subredditInfo(subreddit)
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    override fun loadPage() {
        viewModelScope.launch { submissionRepo.loadPage(subreddit) }
    }

    override fun hasNextPage() = submissionRepo.hasNextPage()

    fun reload() {
        submissionRepo.clearSubmissions()
        loadPage()
    }

    fun setPostViewType(type: SubmissionUiType) {
        viewModelScope.launch { submissionRepo.setPostViewType(subreddit, type) }
    }

    fun changeSort(newSubredditSorting: SubredditSorting) {
        viewModelScope.launch { submissionRepo.changeSort(subreddit, newSubredditSorting) }
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