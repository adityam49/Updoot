package com.ducktapedapps.updoot.ui.subreddit

import com.ducktapedapps.updoot.utils.Page
import com.ducktapedapps.updoot.utils.PostViewType
import kotlinx.coroutines.flow.StateFlow

interface SubredditVM {
    val subredditName: String

    val sorting: StateFlow<SubredditSorting>

    val postViewType: StateFlow<PostViewType>

    val pagesOfPosts: StateFlow<List<Page<PostUiModel>>>

    val subredditInfo: StateFlow<SubredditInfoState?>

    fun loadPage()

    fun reload()

    fun loadSubredditInfo()

    fun setPostViewType(type: PostViewType)

    fun changeSorting(newSubredditSorting: SubredditSorting)

    fun upVote(id: String)

    fun downVote(id: String)

    fun save(id: String)
}