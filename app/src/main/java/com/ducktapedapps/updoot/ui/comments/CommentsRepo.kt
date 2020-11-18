package com.ducktapedapps.updoot.ui.comments

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ducktapedapps.updoot.data.local.model.Comment
import com.ducktapedapps.updoot.data.local.model.Comment.CommentData
import com.ducktapedapps.updoot.data.local.model.Comment.MoreCommentData
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CommentsRepo @Inject constructor(
        private val redditClient: RedditClient
) {
    private val _allComments = MutableStateFlow<List<Comment>>(emptyList())
    val visibleComments: Flow<List<Comment>> = _allComments

    private val _commentsAreLoading = MutableLiveData(true)
    val commentsAreLoading: LiveData<Boolean> = _commentsAreLoading

    suspend fun loadComments(subreddit: String, submission_id: String) {
        withContext(Dispatchers.IO) {
            try {
                val redditAPI = redditClient.api()
                val response = (redditAPI.getComments(subreddit, submission_id.removePrefix("t3_")))
                val allComments = response[1].children.filterIsInstance(Comment::class.java)
                withContext(Dispatchers.Main) { _allComments.value = allComments }
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                _commentsAreLoading.postValue(false)
            }
        }
    }

    fun toggleChildrenCommentVisibility(index: Int) {
        val listOfComments = _allComments.value
        if (listOfComments[index] is CommentData) {
            val updateList = listOfComments.toMutableList()
            val parentComment = listOfComments[index]
            if (parentComment is CommentData) {
                val replies = parentComment.replies.children
                if (replies.isNotEmpty()) {
                    updateList[index] = parentComment.copy(repliesExpanded = !parentComment.repliesExpanded)
                    if (!parentComment.repliesExpanded) {
                        updateList.addAll(index + 1, recursiveChildrenExpansion(replies))
                    } else {
                        if (replies.isNotEmpty()) {
                            val commentsToBeRemoved = mutableListOf<Comment>()
                            for (i in index + 1 until updateList.size) {
                                when (val comment = updateList[i]) {
                                    is CommentData -> if (comment.depth > parentComment.depth) commentsToBeRemoved.add(comment)
                                    else break
                                    is MoreCommentData -> if (comment.depth > parentComment.depth) commentsToBeRemoved.add(comment)
                                    else break
                                }
                            }
                            updateList.removeAll(commentsToBeRemoved)
                        }
                    }
                    _allComments.value = updateList
                }
            }
        }
    }

    private fun recursiveChildrenExpansion(list: List<Comment>): List<Comment> =
            mutableListOf<Comment>().apply {
                list.forEach {
                    if (it is CommentData) {
                        this += it.copy(repliesExpanded = !it.repliesExpanded)
                        val replies = it.replies.children
                        if (replies.isNotEmpty()) this += recursiveChildrenExpansion(replies)
                    } else this += it
                }
            }

    suspend fun fetchMoreComments(submissionId: String, moreCommentData: MoreCommentData, index: Int) {
        withContext(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) { _commentsAreLoading.value = true }
                val api = redditClient.api()
                val childCommentTree = mutableListOf<Comment>()
                if (moreCommentData.children.isNotEmpty()) {
                    val results = api.getMoreChildren(
                            link_id = submissionId,
                            children = TextUtils.join(",", moreCommentData.children)
                    )
                    childCommentTree += buildCommentForestFromFlattenedComments(results.children)
                    val parentCommentIndex = _allComments.value.indexOfFirst { moreCommentData.parent_id == it.name }
                    val parentCommentData = _allComments.value[parentCommentIndex] as CommentData
                    _allComments.value = _allComments.value
                            .toMutableList()
                            .apply {
                                removeAt(index)
                                this[parentCommentIndex] = parentCommentData.copy(
                                        repliesExpanded = false,
                                        replies = parentCommentData.replies.copy(children = childCommentTree)
                                )

                            }.toList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                withContext(Dispatchers.Main) { _commentsAreLoading.value = false }
            }
        }
    }

    private fun buildCommentForestFromFlattenedComments(flattenedComments: List<Comment>): List<Comment> {
        val map = mutableMapOf<String, Comment>()
        flattenedComments.apply {
            forEach { map[it.name] = it }
            forEach { comment ->
                when (comment) {
                    is CommentData -> {
                        val commentParent: CommentData? = map[comment.parent_id] as? CommentData
                        if (commentParent != null)
                            map[comment.parent_id] = commentParent.copy(replies = commentParent.replies.addChildren(listOf(comment)))
//                        else resultCommentForest += comment
                    }
                    is MoreCommentData -> {
//                        resultCommentForest += comment
                    }
                }
            }
        }
        val minDepth = map.values.first().depth
        return map.values.filter { it.depth == minDepth }
    }
}

