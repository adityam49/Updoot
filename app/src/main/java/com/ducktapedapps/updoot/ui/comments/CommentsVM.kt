package com.ducktapedapps.updoot.ui.comments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CommentsVM(application: Application, id: String, subreddit_name: String) : AndroidViewModel(application) {
    private val repo = CommentsRepo(application)
    val allComments = repo.allComments
    val isLoading = repo.isLoading

    init {
        loadComments(subreddit_name, id)
    }

    private fun loadComments(subreddit: String, submission_id: String) {
        viewModelScope.launch {
            repo
                    .loadComments(subreddit, submission_id)
        }
    }

    fun toggleChildrenVisibility(index: Int) {
        viewModelScope.launch {
            repo.toggleChildrenCommentVisibility(index)
        }
    }

    fun castVote(direction: Int, index: Int) {
        viewModelScope.launch {
            repo.castVote(direction, index)
        }
    }

}

