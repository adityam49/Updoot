package com.ducktapedapps.updoot.ui.comments

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ducktapedapps.updoot.api.remote.RedditAPI
import com.ducktapedapps.updoot.model.BaseComment
import com.ducktapedapps.updoot.model.CommentData
import com.ducktapedapps.updoot.model.MoreCommentData
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import com.ducktapedapps.updoot.utils.linkMetaData.LinkModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CommentsRepo @Inject constructor(
        private val redditClient: RedditClient
) {

    private val _allComments = MutableLiveData<List<BaseComment>>()
    val allComments: LiveData<List<BaseComment>> = _allComments

    private val _commentsAreLoading = MutableLiveData(true)
    val commentsAreLoading: LiveData<Boolean> = _commentsAreLoading

    suspend fun loadComments(subreddit: String, submission_id: String) {
        withContext(Dispatchers.IO) {
            try {
                val redditAPI = redditClient.api()
                val response = redditAPI.getComments(subreddit, submission_id).comments
                if (response.isNotEmpty()) {
                    _allComments.postValue(response)
                } else
                    Log.e(this.javaClass.simpleName, "response from retrofit is empty")
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                _commentsAreLoading.postValue(false)
            }
        }
    }

    fun toggleChildrenCommentVisibility(index: Int, link_id: String) {
        if (_allComments.value?.get(index) is CommentData) {
            _allComments.value?.let {
                val list = it.toMutableList()
                val parentComment = list[index]
                if (parentComment is CommentData) {
                    if (!parentComment.replies.isNullOrEmpty()) {
                        list[index] = parentComment.copy(repliesExpanded = !parentComment.repliesExpanded)
                        if (!parentComment.repliesExpanded) {
                            list.addAll(index + 1, recursiveChildrenExpansion(parentComment.replies))
                        } else {
                            if (parentComment.replies.isNotEmpty()) {
                                val commentsToBeRemoved = mutableListOf<BaseComment>()
                                for (i in index + 1 until list.size) {
                                    if (list[i].depth > parentComment.depth) commentsToBeRemoved.add(list[i])
                                    else break
                                }
                                list.removeAll(commentsToBeRemoved)
                            }
                        }
                        _allComments.postValue(list)
                    }
                }
            }
        }
    }

    private fun recursiveChildrenExpansion(list: List<BaseComment>): List<BaseComment> =
            mutableListOf<BaseComment>().apply {
                list.forEach {
                    if (it is CommentData) {
                        this += it.copy(repliesExpanded = !it.repliesExpanded)
                        if (it.replies.isNotEmpty()) this += recursiveChildrenExpansion(it.replies)
                    } else this += it
                }
            }
}

