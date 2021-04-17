package com.ducktapedapps.updoot.comments

import android.text.TextUtils
import com.ducktapedapps.updoot.data.local.model.FullComment
import com.ducktapedapps.updoot.data.local.model.LocalComment
import com.ducktapedapps.updoot.data.local.model.MoreComment
import com.ducktapedapps.updoot.data.mappers.toLocalComment
import com.ducktapedapps.updoot.data.remote.model.Comment
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CommentsRepo @Inject constructor(
    private val redditClient: RedditClient
) {
    private val _allComments = MutableStateFlow<List<LocalComment>>(emptyList())
    val visibleComments: Flow<List<LocalComment>> = _allComments

    val commentsAreLoading = MutableStateFlow(true)

    suspend fun loadComments(subreddit: String, submission_id: String) {
        withContext(Dispatchers.IO) {
            try {
                val redditAPI = redditClient.api()
                val response = (redditAPI.getComments(subreddit, submission_id.removePrefix("t3_")))
                val allComments = response[1].children.filterIsInstance(Comment::class.java)
                withContext(Dispatchers.Main) { _allComments.value = allComments.map { it.toLocalComment() } }
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                commentsAreLoading.value = false
            }
        }
    }

    fun toggleChildrenCommentVisibility(index: Int) {
        val listOfComments = _allComments.value
        if (listOfComments[index] is FullComment) {
            val updateList = listOfComments.toMutableList()
            val parentComment = listOfComments[index]
            if (parentComment is FullComment) {
                val replies = parentComment.replies
                if (replies.isNotEmpty()) {
                    updateList[index] = parentComment.copy(repliesExpanded = !parentComment.repliesExpanded)
                    if (!parentComment.repliesExpanded) {
                        updateList.addAll(index + 1, recursiveChildrenExpansion(replies))
                    } else {
                        if (replies.isNotEmpty()) {
                            val commentsToBeRemoved = mutableListOf<LocalComment>()
                            for (i in index + 1 until updateList.size) {
                                when (val comment = updateList[i]) {
                                    is FullComment -> if (comment.depth > parentComment.depth) commentsToBeRemoved.add(comment)
                                    else break
                                    is MoreComment -> if (comment.depth > parentComment.depth) commentsToBeRemoved.add(comment)
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

    private fun recursiveChildrenExpansion(list: List<LocalComment>): List<LocalComment> =
            mutableListOf<LocalComment>().apply {
                list.forEach {
                    if (it is FullComment) {
                        this += it.copy(repliesExpanded = !it.repliesExpanded)
                        val replies = it.replies
                        if (replies.isNotEmpty()) this += recursiveChildrenExpansion(replies)
                    } else this += it
                }
            }

    suspend fun fetchMoreComments(submissionId: String, moreCommentData: MoreComment, index: Int) {
        withContext(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) { commentsAreLoading.value = true }
                val api = redditClient.api()
                val childCommentTree = mutableListOf<LocalComment>()
                if (moreCommentData.children.isNotEmpty()) {
                    val results = api.getMoreChildren(
                            link_id = submissionId,
                            children = TextUtils.join(",", moreCommentData.children)
                    )
                    childCommentTree += buildCommentForestFromFlattenedComments(results.children.map { it.toLocalComment() })
                    val parentCommentIndex = _allComments.value.indexOfFirst { moreCommentData.parentId == it.id }
                    val parentCommentData = _allComments.value[parentCommentIndex] as FullComment
                    _allComments.value = _allComments.value
                            .toMutableList()
                            .apply {
                                removeAt(index)
                                this[parentCommentIndex] = parentCommentData.copy(
                                        repliesExpanded = false,
                                        replies = childCommentTree
                                )

                            }.toList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                withContext(Dispatchers.Main) { commentsAreLoading.value = false }
            }
        }
    }

    private fun buildCommentForestFromFlattenedComments(flattenedComments: List<LocalComment>): List<LocalComment> {
        val map = mutableMapOf<String, LocalComment>()
        flattenedComments.apply {
            forEach { map[it.id] = it }
            forEach { comment ->
                when (comment) {
                    is FullComment -> {
                        val commentParent: FullComment? = map[comment.parentId] as? FullComment
                        if (commentParent != null)
                            map[comment.parentId] = commentParent.copy(replies = commentParent.replies + listOf(comment))
//                        else resultCommentForest += comment
                    }
                    is MoreComment -> {
//                        resultCommentForest += comment
                    }
                }
            }
        }
        val minDepth = map.values.first().depth
        return map.values.filter { it.depth == minDepth }
    }
}

