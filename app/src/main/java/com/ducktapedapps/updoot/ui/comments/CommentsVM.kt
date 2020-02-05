package com.ducktapedapps.updoot.ui.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CommentsVM(private val repo: CommentsRepo, val id: String, subreddit_name: String) : ViewModel() {
    val allComments = repo.allComments
    val isLoading = repo.isLoading

    init {
        loadComments(subreddit_name, id)
    }

    private fun loadComments(subreddit: String, submission_id: String) {
        viewModelScope.launch {
            repo.loadComments(subreddit, submission_id)
        }
    }

    fun toggleChildrenVisibility(index: Int) {
        viewModelScope.launch {
            repo.toggleChildrenCommentVisibility(index, id)
        }
    }

    fun castVote(direction: Int, index: Int) {
        viewModelScope.launch {
            repo.castVote(direction, index)
        }
    }

}

