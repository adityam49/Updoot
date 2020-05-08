package com.ducktapedapps.updoot.ui.comments

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ducktapedapps.updoot.api.remote.RedditAPI
import com.ducktapedapps.updoot.model.BaseComment
import com.ducktapedapps.updoot.model.CommentData
import com.ducktapedapps.updoot.model.MoreCommentData
import com.ducktapedapps.updoot.utils.accountManagement.Reddit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CommentsRepo @Inject constructor(val reddit: Reddit) {

    private val _allComments = MutableLiveData<List<BaseComment>>()
    private val _isLoading = MutableLiveData(true)

    val allComments: LiveData<List<BaseComment>> = _allComments
    val isLoading: LiveData<Boolean> = _isLoading

    suspend fun loadComments(subreddit: String, submission_id: String) {
        withContext(Dispatchers.IO) {
            try {
                val redditAPI = reddit.authenticatedAPI()
                try {
                    val response = redditAPI.getComments(subreddit, submission_id).comments
                    if (response.isNotEmpty()) {
                        _allComments.postValue(response)
                    } else
                        Log.e(this.javaClass.simpleName, "response from retrofit is empty")
                } catch (ex: Exception) {
                    Log.e("commentsRepo", "couldn't fetch comments ", ex)
                }
            } catch (ex: Exception) {
                Log.e("commentsRepo", "couldn't get reddit api ", ex)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    suspend fun toggleChildrenCommentVisibility(index: Int, link_id: String) {
        if (_allComments.value?.get(index) is MoreCommentData) {
            val moreCommentObject = _allComments.value?.get(index) as MoreCommentData
            loadMoreChildren(link_id, moreCommentObject.children)
        } else
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

    private suspend fun loadMoreChildren(link_id: String, children_ids: List<String>) {
        withContext(Dispatchers.IO) {
            _isLoading.postValue(true)
            try {
                val api = reddit.authenticatedAPI()
                val result = api.getMoreChildren(
                        children_ids.joinToString(","),
                        "t3_$link_id"
                )
                //TODO : merge loaded moreComments to parent comment tree
            } catch (ex: Exception) {
                Log.e(this.javaClass.simpleName, "unable to load more comments", ex)
            } finally {
                _isLoading.postValue(false)
            }
        }

    }

    private fun recursiveChildrenExpansion(list: List<BaseComment>): List<BaseComment> {
        val updateList = mutableListOf<BaseComment>()
        for (comment in list) {
            if (comment is CommentData) {
                updateList.add(comment.copy(repliesExpanded = !comment.repliesExpanded))
                if (comment.replies.isNotEmpty()) updateList.addAll(recursiveChildrenExpansion(comment.replies))
            } else updateList += comment
        }
        return updateList
    }


    suspend fun castVote(direction: Int, index: Int) {
        withContext(Dispatchers.IO) {
            try {
                val redditAPI: RedditAPI? = reddit.authenticatedAPI()
                if (redditAPI != null) {
                    try {
                        _allComments.value?.let {
                            if (it[index] is CommentData) {
                                val intendedDirection = when (direction) {
                                    1 -> if ((it[index] as CommentData).likes != true) 1 else 0
                                    -1 -> if ((it[index] as CommentData).likes != false) -1 else 0
                                    else -> direction
                                }
                                val result = redditAPI.castVote(it[index].name, intendedDirection)
                                if (result == "{}") {
                                    Log.i(this.javaClass.simpleName, "casting vote : success $result")
                                    val updateCommentList = it.toMutableList()
                                    updateCommentList[index] = (it[index] as CommentData).vote(direction)
                                    _allComments.postValue(updateCommentList)
                                } else {
                                    throw Exception(result)
                                }
                            }

                        }
                    } catch (exception: Exception) {
                        Log.e(this.javaClass.simpleName, "could not cast vote : ", exception)
                    }
                } else throw Exception("Unable to get reddit api")

            } catch (ex: Exception) {
                Log.e(this.javaClass.simpleName, "unable to get reddit api", ex)
            }
        }
    }
}

