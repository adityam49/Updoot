package com.ducktapedapps.updoot.comments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.data.local.LinkMetaDataDAO
import com.ducktapedapps.updoot.data.local.PostDAO
import com.ducktapedapps.updoot.data.local.model.LocalComment
import com.ducktapedapps.updoot.data.local.model.MoreComment
import com.ducktapedapps.updoot.subreddit.PostUiModel
import com.ducktapedapps.updoot.subreddit.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentsVMImpl @Inject constructor(
    private val repo: CommentsRepo,
    postCacheDAO: PostDAO,
    prefManager: CommentPrefManager,
) : ViewModel(), CommentsVM {

    private val pageKey: MutableStateFlow<Pair<String, String>?> = MutableStateFlow(null)
    fun setPageKey(subredditID: String, postID: String) {
        pageKey.value = Pair(subredditID, postID)
    }

    private val singleThreadMode: StateFlow<Boolean> = prefManager
        .showSingleThread()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    private val singleColorThreadMode: StateFlow<Boolean> = prefManager
        .showSingleThreadColor()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val comments =
        repo.visibleComments.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val post = pageKey
        .filterNotNull()
        .flatMapLatest { ids ->
            loadComments()
            postCacheDAO
                .observePost(ids.second)
                .map { it.toUiModel() }
                .distinctUntilChanged()
        }

    private val isLoading = repo.commentsAreLoading

    override val viewState: StateFlow<ViewState> = combine(
        comments, post, isLoading, singleThreadMode, singleColorThreadMode
    ) { commentsValue, postValue, isLoadingValue, singleThreadModeValue, singleColorThreadValue ->
        ViewState(
            comments = commentsValue,
            post = postValue,
            isLoading = isLoadingValue,
            isSingleThreadMode = singleThreadModeValue,
            isSingleColorThread = singleColorThreadValue
        )
    }.stateIn(
        viewModelScope, SharingStarted.Lazily, ViewState(
            comments = emptyList(),
            isLoading = false,
            isSingleColorThread = false,
            isSingleThreadMode = false,
            post = null
        )
    )

    init {
        loadComments()
    }

    private fun loadComments() {
        viewModelScope.launch {
            pageKey.value?.let {
                repo.loadComments(it.first, it.second)
            }
        }
    }

    override fun toggleChildrenVisibility(index: Int) {
        viewModelScope.launch {
            repo.toggleChildrenCommentVisibility(index)
        }
    }

    override fun loadMoreComment(moreComment: MoreComment, index: Int) {
        viewModelScope.launch {
            pageKey.value?.let {
                repo.fetchMoreComments(it.second, moreComment, index)
            }
        }
    }

    override fun castVote(direction: Int, index: Int) = Unit
}

data class ViewState(
    val comments: List<LocalComment>,
    val isLoading: Boolean,
    val post: PostUiModel?,
    val isSingleThreadMode: Boolean,
    val isSingleColorThread: Boolean
)