package com.ducktapedapps.updoot.ui.subreddit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.ui.InfiniteScrollVM
import com.ducktapedapps.updoot.utils.SingleLiveEvent
import kotlinx.coroutines.launch

class SubmissionsVM(application: Application, val subreddit: String) : AndroidViewModel(application), InfiniteScrollVM {
    private val frontPageRepo: SubmissionRepo = SubmissionRepo(application)
    override val isLoading = frontPageRepo.isLoading

    private var sorting: String?
    private var time: String?

    val allSubmissions: LiveData<MutableList<LinkData>> = frontPageRepo.allSubmissions
    val toastMessage: LiveData<SingleLiveEvent<String?>> = frontPageRepo.toastMessage

    override fun loadPage(appendPage: Boolean) {
        viewModelScope.launch {
            frontPageRepo.loadPage(subreddit, sorting, time, appendPage)
        }
    }

    override fun hasNextPage(): Boolean {
        return frontPageRepo.after != null
    }

    fun castVote(index: Int, direction: Int) {
        viewModelScope.launch {
            frontPageRepo.castVote(index, direction)
        }
    }

    fun toggleSave(index: Int) {
        viewModelScope.launch {
            frontPageRepo.save(index)
        }
    }

    fun reload(sorting: String?, time: String?) {
        this.sorting = sorting
        this.time = time
        frontPageRepo.after = null
        viewModelScope.launch { loadPage(false) }
    }


    fun expandSelfText(index: Int) {
        frontPageRepo.expandSelfText(index)
    }

    init {
        time = null
        sorting = null
        viewModelScope.launch {
            loadPage(false)
        }
    }
}