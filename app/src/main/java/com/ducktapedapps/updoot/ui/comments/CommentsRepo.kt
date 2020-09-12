package com.ducktapedapps.updoot.ui.comments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ducktapedapps.updoot.data.local.model.BaseComment
import com.ducktapedapps.updoot.data.local.model.CommentData
import com.ducktapedapps.updoot.data.local.model.MoreCommentData
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import com.ducktapedapps.updoot.utils.asCommentPage
import com.ducktapedapps.updoot.utils.mapToRepliesModel
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
                val response = (redditAPI.getComments(subreddit, submission_id))
                val allComments = response.asCommentPage().component2()
                _allComments.postValue(allComments)
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
                    val replies = parentComment.replies.mapToRepliesModel()
                    if (replies.isNotEmpty()) {
                        list[index] = parentComment.copy(repliesExpanded = !parentComment.repliesExpanded)
                        if (!parentComment.repliesExpanded) {
                            list.addAll(index + 1, recursiveChildrenExpansion(replies))
                        } else {
                            if (!replies.isNullOrEmpty()) {
                                val commentsToBeRemoved = mutableListOf<BaseComment>()
                                for (i in index + 1 until list.size) {
                                    when (val comment = list[i]) {
                                        is CommentData -> if (comment.depth > parentComment.depth) commentsToBeRemoved.add(comment)
                                        else break
                                        is MoreCommentData -> if (comment.depth > parentComment.depth) commentsToBeRemoved.add(comment)
                                        else break
                                    }
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
                        val replies = it.replies.mapToRepliesModel()
                        if (replies.isNotEmpty()) this += recursiveChildrenExpansion(replies)
                    } else this += it
                }
            }
}

