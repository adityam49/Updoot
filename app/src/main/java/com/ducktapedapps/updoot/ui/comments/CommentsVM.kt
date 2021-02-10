package com.ducktapedapps.updoot.ui.comments

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.data.local.LinkMetaDataDAO
import com.ducktapedapps.updoot.data.local.PostDAO
import com.ducktapedapps.updoot.data.local.model.MoreComment
import com.ducktapedapps.updoot.ui.subreddit.toUiModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CommentsVMImpl @ViewModelInject constructor(
        private val repo: CommentsRepo,
        postCacheDAO: PostDAO,
        private val linkMetaDataDAO: LinkMetaDataDAO,
        @Assisted savedStateHandle: SavedStateHandle,
        prefManager: ICommentPrefManager,
) : ViewModel(), ICommentsVM {

    private val id: String = savedStateHandle.get<String>(CommentsFragment.COMMENTS_KEY)!!
    private val subreddit: String = savedStateHandle.get<String>(CommentsFragment.SUBREDDIT_KEY)!!
    override val singleThreadMode: StateFlow<Boolean> = prefManager
            .showSingleThread()
            .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    override val singleColorThreadMode: StateFlow<Boolean> = prefManager
            .showSingleThreadColor()
            .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    override val comments = repo.visibleComments.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    override val post = postCacheDAO
            .observePost(id)
            .map { it.toUiModel() }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    override val isLoading = repo.commentsAreLoading

    init {
        loadComments()
    }

    private fun loadComments() {
        viewModelScope.launch {
            repo.loadComments(subreddit, id)
        }
    }

    override fun toggleChildrenVisibility(index: Int) {
        viewModelScope.launch {
            repo.toggleChildrenCommentVisibility(index)
        }
    }

    override fun loadMoreComment(moreComment: MoreComment, index: Int) {
        viewModelScope.launch {
            repo.fetchMoreComments(id, moreComment, index)
        }
    }

    override fun castVote(direction: Int, index: Int) = Unit

    //TODO : fix link metadata fetching model
//    private fun getMetaDataFor(url: String): Flow<LinkState> = flow {
//        emit(LinkState.LoadingLink(url))
//        emitAll(url
//                .fetchMetaData(linkMetaDataDAO)
//                .catch { error -> emit(LinkState.NoMetaDataLink(url, error.message ?: "")) }
//                .map { LinkState.LoadedLink(it) })
//    }
}