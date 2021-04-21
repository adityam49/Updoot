package com.ducktapedapps.updoot.subreddit

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.utils.PagingModel
import com.ducktapedapps.updoot.utils.PagingModel.Footer.Loading
import com.ducktapedapps.updoot.utils.PostViewType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

interface SubredditVM {
    val subredditName: String

    val sorting: StateFlow<SubredditSorting>

    val postViewType: StateFlow<PostViewType>

    val pagesOfPosts: StateFlow<PagingModel<List<PostUiModel>>>

    val subredditInfo: StateFlow<SubredditInfoState?>

    val subscriptionState: StateFlow<Boolean?>

    fun loadPage()

    fun reload()

    fun loadSubredditInfo()

    fun toggleSubredditSubscription()

    fun setPostViewType(type: PostViewType)

    fun changeSorting(newSubredditSorting: SubredditSorting)

    fun upVote(id: String)

    fun downVote(id: String)

    fun save(id: String)
}


class SubredditVMImpl @ViewModelInject constructor(
    @Assisted savedStateHandle: SavedStateHandle,
    getSubredditPreferencesUseCase: GetSubredditPreferencesUseCase,
    private val getSubredditInfoUseCase: GetSubredditInfoUseCase,
    private val getSubredditPostsUseCase: GetSubredditPostsUseCase,
    private val setSubredditViewTypeUseCase: SetSubredditPostViewTypeUseCase,
    private val toggleSubscriptionUseCase: EditSubredditSubscriptionUseCase,
    getSubredditSubscriptionState: GetSubredditSubscriptionState,
) : ViewModel(), SubredditVM {
    override val subredditName: String =
        savedStateHandle.get<String>(SubredditFragment.SUBREDDIT_KEY)!!

    override val sorting: StateFlow<SubredditSorting> = getSubredditPreferencesUseCase
        .getSubredditPrefsFlow(subredditName)
        .map { it.subredditSorting }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SubredditSorting.Hot
        )

    override val postViewType: StateFlow<PostViewType> = getSubredditPreferencesUseCase
        .getSubredditPrefsFlow(subredditName)
        .map { it.viewType }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = PostViewType.COMPACT
        )

    override val pagesOfPosts: StateFlow<PagingModel<List<PostUiModel>>> =
        getSubredditPostsUseCase
            .pagingModel
            .map { model ->
                PagingModel(
                    model.content.map { post -> post.toUiModel() },
                    model.footer
                )
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, PagingModel(emptyList(), Loading))

    override val subredditInfo: StateFlow<SubredditInfoState?> =
        getSubredditInfoUseCase.subredditInfo

    override val subscriptionState: StateFlow<Boolean?> =
        getSubredditSubscriptionState.getIsSubredditSubscribedState(subredditName)
            .stateIn(
                viewModelScope, SharingStarted.WhileSubscribed(), null
            )

    override fun loadSubredditInfo() {
        viewModelScope.launch { getSubredditInfoUseCase.loadSubredditInfo(subredditName) }
    }

    init {
        sorting
            .filterNotNull()
            .onEach { reload() }
            .launchIn(viewModelScope)
        loadSubredditInfo()
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

    override fun toggleSubredditSubscription() {
        viewModelScope.launch {
            toggleSubscriptionUseCase.toggleSubscription(subredditName = subredditName)
        }
    }

    override fun changeSorting(newSubredditSorting: SubredditSorting) {
        //TODO
    }

    override fun upVote(id: String) {}

    override fun downVote(id: String) {}

    override fun save(id: String) {}
}
