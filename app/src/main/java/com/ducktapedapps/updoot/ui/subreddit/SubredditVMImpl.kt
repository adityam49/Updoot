package com.ducktapedapps.updoot.ui.subreddit

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.ui.subreddit.SubredditSorting.Hot
import com.ducktapedapps.updoot.utils.Page
import com.ducktapedapps.updoot.utils.PostViewType
import com.ducktapedapps.updoot.utils.PostViewType.COMPACT
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SubredditVMImpl @ViewModelInject constructor(
    @Assisted savedStateHandle: SavedStateHandle,
    private val getSubredditPreferencesUseCase: GetSubredditPreferencesUseCase,
    private val getSubredditInfoUseCase: GetSubredditInfoUseCase,
    private val getSubredditPostsUseCase: GetSubredditPostsUseCase,
    private val setSubredditViewTypeUseCase: SetSubredditPostViewTypeUseCase,
) : ViewModel(), SubredditVM {
    override val subredditName: String =
        savedStateHandle.get<String>(SubredditFragment.SUBREDDIT_KEY)!!

    override val sorting: StateFlow<SubredditSorting> = getSubredditPreferencesUseCase
        .getSubredditPrefsFlow(subredditName)
        .map { it.subredditSorting }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = Hot
        )

    override val postViewType: StateFlow<PostViewType> = getSubredditPreferencesUseCase
        .getSubredditPrefsFlow(subredditName)
        .map { it.viewType }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = COMPACT
        )

    override val pagesOfPosts: StateFlow<List<Page<PostUiModel>>> =
        getSubredditPostsUseCase.pagesOfPosts

    override val subredditInfo: StateFlow<SubredditInfoState?> =
        getSubredditInfoUseCase.subredditInfo

    override fun loadSubredditInfo() {
        viewModelScope.launch { getSubredditInfoUseCase.loadSubredditInfo(subredditName) }
    }

    init {
        sorting
            .filterNotNull()
            .onEach { reload() }
            .launchIn(viewModelScope)
        viewModelScope.launch {
            loadSubredditInfo()
        }
    }


    override fun loadPage() {
        viewModelScope.launch {
            getSubredditPostsUseCase.loadNextPage(
                subredditSorting = sorting.value,
                subreddit = subredditName
            )
        }
    }

    override fun reload() {
        viewModelScope.launch {
            getSubredditPostsUseCase.loadNextPage(
                reload = true,
                subredditSorting = sorting.value,
                subreddit = subredditName
            )
        }
    }

    override fun setPostViewType(type: PostViewType) {
        viewModelScope.launch {
            setSubredditViewTypeUseCase.setPostViewType(subredditName, type)
        }
    }

    override fun changeSorting(newSubredditSorting: SubredditSorting) {
        //TODO
    }

    override fun upVote(id: String) {}

    override fun downVote(id: String) {}

    override fun save(id: String) {}
}