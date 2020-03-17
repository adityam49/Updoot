package com.ducktapedapps.updoot.ui.subreddit

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.ui.InfiniteScrollVM
import com.ducktapedapps.updoot.utils.SingleLiveEvent
import com.ducktapedapps.updoot.utils.Sorting
import kotlinx.coroutines.launch

class SubmissionsVM constructor(val subreddit: String, private val submissionRepo: SubmissionRepo) : ViewModel(), InfiniteScrollVM {
    override val isLoading = submissionRepo.isLoading

    private var sorting: Sorting
    private var time: String?

    val allSubmissions: LiveData<MutableList<LinkData>> = submissionRepo.allSubmissions
    val toastMessage: LiveData<SingleLiveEvent<String?>> = submissionRepo.toastMessage

    override fun loadPage(appendPage: Boolean) {
        viewModelScope.launch {
            submissionRepo.loadPage(subreddit, sorting, time, appendPage)
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
     * Clears the cached data and fetches data with passed in parameters
     * @param sorting if null uses the cached sorting method
     */
    fun reload(sorting: Sorting?, time: String?) {
        this.sorting = sorting ?: this.sorting
        this.time = time
        submissionRepo.after = null
        viewModelScope.launch { loadPage(false) }
    }


    fun expandSelfText(index: Int) {
        submissionRepo.expandSelfText(index)
    }

    init {
        time = null
        sorting = Sorting.HOT
        viewModelScope.launch {
            loadPage(false)
        }
    }
}