package com.ducktapedapps.updoot.ui.subreddit

import com.ducktapedapps.updoot.data.local.model.LocalSubreddit
import com.ducktapedapps.updoot.utils.SubmissionUiType
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface ISubredditVM {
    val subredditName: String

    val isLoading: StateFlow<Boolean>

    val toastMessage: SharedFlow<String>

    val sorting: StateFlow<SubredditSorting?>

    val postViewType: StateFlow<SubmissionUiType>

    val feedPages: StateFlow<List<PostUiModel>>

    var lastScrollPosition: Int

    val subredditInfo: StateFlow<LocalSubreddit?>

    fun loadPage()

    fun hasNextPage(): Boolean

    fun reload()

    fun setPostViewType(type: SubmissionUiType)

    fun changeSort(newSubredditSorting: SubredditSorting)

    fun upVote(id: String)

    fun downVote(id: String)

    fun save(id: String)
}