package com.ducktapedapps.updoot.comments

import com.ducktapedapps.updoot.data.local.model.LocalComment
import com.ducktapedapps.updoot.data.local.model.MoreComment
import com.ducktapedapps.updoot.subreddit.PostUiModel
import kotlinx.coroutines.flow.StateFlow

interface CommentsVM {

    val viewState: StateFlow<ViewState>

    fun toggleChildrenVisibility(index: Int)

    fun loadMoreComment(moreComment: MoreComment, index: Int)

    fun castVote(direction: Int, index: Int) = Unit

}

