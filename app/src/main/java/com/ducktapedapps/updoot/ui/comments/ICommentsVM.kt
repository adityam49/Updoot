package com.ducktapedapps.updoot.ui.comments

import com.ducktapedapps.updoot.data.local.model.LocalComment
import com.ducktapedapps.updoot.data.local.model.MoreComment
import com.ducktapedapps.updoot.ui.subreddit.PostUiModel
import kotlinx.coroutines.flow.StateFlow

interface ICommentsVM {

    val comments: StateFlow<List<LocalComment>>

    val isLoading: StateFlow<Boolean>

    val post: StateFlow<PostUiModel?>

    val singleThreadMode: StateFlow<Boolean>

    val singleColorThreadMode: StateFlow<Boolean>

    fun toggleChildrenVisibility(index: Int)

    fun loadMoreComment(moreComment: MoreComment, index: Int)

    fun castVote(direction: Int, index: Int) = Unit

}