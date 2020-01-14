package com.ducktapedapps.updoot.ui.comments

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.api.RedditAPI
import com.ducktapedapps.updoot.model.CommentData
import com.ducktapedapps.updoot.utils.accountManagement.Reddit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentsRepo(application: Application) {

    init {
        (application as UpdootApplication).updootComponent.inject(this)
    }

    @Inject
    lateinit var reddit: Reddit

    private val _allComments = MutableLiveData<List<CommentData>>()
    private val _isLoading = MutableLiveData(true)

    val allComments: LiveData<List<CommentData>> = _allComments
    val isLoading: LiveData<Boolean> = _isLoading

    suspend fun loadComments(subreddit: String, submission_id: String) {
        withContext(Dispatchers.IO) {
            try {
                val redditAPI = reddit.authenticatedAPI()
                try {
                    val response = redditAPI.getComments(subreddit, submission_id)
                    if (response != null)
                        _allComments.postValue(response.commentList)
                    else
                        Log.e(this.javaClass.simpleName, "response from retrofit is null")
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

    suspend fun toggleChildrenCommentVisibility(index: Int) {
        withContext(Dispatchers.Default) {
            _allComments.value?.let {
                val list = it.toMutableList()
                val parentComment = list[index]

                if (!parentComment.replies?.commentList.isNullOrEmpty()) {
                    list[index] = parentComment.copy(repliesExpanded = !parentComment.repliesExpanded)
                    if (!parentComment.repliesExpanded) {
                        list.addAll(index + 1, recursiveChildrenExpansion(parentComment.replies?.commentList
                                ?: listOf()))
                    } else {
                        if (!parentComment.replies?.commentList.isNullOrEmpty()) {
                            val commentsToBeRemoved = mutableListOf<CommentData>()
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


    private fun recursiveChildrenExpansion(list: List<CommentData>): List<CommentData> {
        val updateList = mutableListOf<CommentData>()
        for (comment in list) {
            updateList.add(comment.copy(repliesExpanded = !comment.repliesExpanded))
            if (!comment.replies?.commentList.isNullOrEmpty()) updateList.addAll(recursiveChildrenExpansion(comment.replies?.commentList
                    ?: listOf()))
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
                            val intendedDirection = when (direction) {
                                1 -> if (it[index].likes != true) 1 else 0
                                -1 -> if (it[index].likes != false) -1 else 0
                                else -> direction
                            }
                            val result = redditAPI.castVote(it[index].name, intendedDirection)
                            if (result == "{}") {
                                Log.i(this.javaClass.simpleName, "casting vote : success $result")
                                val updateCommentList = it.toMutableList()
                                updateCommentList[index] = it[index].vote(direction)
                                _allComments.postValue(updateCommentList)
                            } else {
                                throw Exception(result)
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

