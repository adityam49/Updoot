package com.ducktapedapps.updoot.ui.subreddit

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.model.Subreddit
import com.ducktapedapps.updoot.ui.InfiniteScrollVM
import com.ducktapedapps.updoot.utils.SingleLiveEvent
import com.ducktapedapps.updoot.utils.Sorting
import com.ducktapedapps.updoot.utils.SubmissionUiType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class SubmissionsVM constructor(val subreddit: String, private val submissionRepo: SubmissionRepo) : ViewModel(), InfiniteScrollVM {
    private val TAG = "SubmissionsVM"
    override val isLoading = submissionRepo.isLoading

    private var time: String? = null

    val subredditInfo: LiveData<Subreddit> = submissionRepo.subredditInfo
    val sorting: LiveData<Sorting> = submissionRepo.sorting
    val uiType: LiveData<SubmissionUiType> = submissionRepo.submissionsUI
    val allSubmissions: LiveData<MutableList<LinkData>> = submissionRepo.allSubmissions
    val toastMessage: LiveData<SingleLiveEvent<String?>> = submissionRepo.toastMessage

    init {
        //Using async as subreddit info is independent of the submission api request
        viewModelScope.launch(Dispatchers.IO) {
            async {
                submissionRepo.loadSubredditPrefs(subreddit)
                loadPage(false)
            }
            async {
//                submissionRepo.loadSubredditInfo(subreddit)
            }
        }

    }

    override fun loadPage(appendPage: Boolean) {
        viewModelScope.launch {
            val sorting = sorting.value
            submissionRepo.loadPage(subreddit = subreddit, sort = sorting
                    ?: Sorting.NO_SORT, time = time, appendPage = appendPage)
        }
    }

    override fun hasNextPage(): Boolean {
        return submissionRepo.after != null
    }

    fun castVote(index: Int, direction: Int) {
        viewModelScope.launch {
            submissionRepo.castVote(index, direction)
        }
    }

    fun toggleSave(index: Int) {
        viewModelScope.launch {
            submissionRepo.save(index)
        }
    }

    /**
     * Clears the cached data and fetches new data
     */
    fun reload() {
        submissionRepo.clearSubmissions()
        viewModelScope.launch { loadPage(false) }
    }


    fun expandSelfText(index: Int) {
        submissionRepo.expandSelfText(index)
    }

    fun toggleUi() {
        viewModelScope.launch(Dispatchers.IO) {
            submissionRepo.toggleUI(subreddit)
        }
    }

    fun changeSort(newSorting: Sorting) {
        viewModelScope.launch((Dispatchers.IO)) {
            submissionRepo.changeSort(newSorting, subreddit)
        }
    }
}