package com.ducktapedapps.updoot.subreddit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.navigation.NavigationDirections
import com.ducktapedapps.updoot.data.local.SubredditPrefs
import com.ducktapedapps.updoot.subreddit.SubredditSorting.Hot
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.PagingModel
import com.ducktapedapps.updoot.utils.PagingModel.Footer.Loading
import com.ducktapedapps.updoot.utils.PostViewType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

interface SubredditVM {

    val viewState: StateFlow<ViewState>

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

@HiltViewModel
class SubredditVMImpl @Inject constructor(
    getSubredditPreferencesUseCase: GetSubredditPreferencesUseCase,
    private val getSubredditInfoUseCase: GetSubredditInfoUseCase,
    private val getSubredditPostsUseCase: GetSubredditPostsUseCase,
    private val setSubredditViewTypeUseCase: SetSubredditPostViewTypeUseCase,
    private val toggleSubscriptionUseCase: EditSubredditSubscriptionUseCase,
    getSubredditSubscriptionState: GetSubredditSubscriptionState,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel(), SubredditVM {
    private val subredditName: StateFlow<String> = savedStateHandle.getStateFlow(
        NavigationDirections.SubredditScreenNavigation.SUBREDDIT_NAME_KEY, ""
    )

    private val subredditPrefs: StateFlow<SubredditPrefs> = subredditName.flatMapLatest { name ->
        getSubredditPreferencesUseCase.getSubredditPrefsFlow(name)
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = SubredditPrefs()
    )

    private val pagesOfPosts: StateFlow<PagingModel<List<PostUiModel>>> =
        getSubredditPostsUseCase.pagingModel.map { model ->
            PagingModel(
                model.content.map { post -> post.toUiModel() }, model.footer
            )
        }.stateIn(viewModelScope, SharingStarted.Lazily, PagingModel(emptyList(), Loading))

    private val subredditInfo: StateFlow<SubredditInfoState?> =
        getSubredditInfoUseCase.subredditInfo

    private val subscriptionState: StateFlow<Boolean?> = subredditName.flatMapLatest { name ->
        getSubredditSubscriptionState.getIsSubredditSubscribedState(name)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)


    override val viewState: StateFlow<ViewState> = combine(
        subredditPrefs, pagesOfPosts, subredditInfo, subscriptionState
    ) { subredditPrefsValue, feed, subredditInfoValue, subscriptionStateValue ->
        val subredditName = if (subredditPrefsValue.subredditName == Constants.FRONTPAGE) {
            "Frontpage"
        } else {
            subredditPrefsValue.subredditName
        }
        ViewState(
            subredditPrefs = subredditPrefsValue.copy(subredditName = subredditName),
            feed = feed,
            subredditInfo = subredditInfoValue,
            subscriptionState = subscriptionStateValue
        )
    }.stateIn(
        viewModelScope, SharingStarted.Lazily, ViewState()
    )

    init {
        combine(subredditName, subredditPrefs) { _, _ ->
            loadPage()
        }.launchIn(viewModelScope)
    }

    override fun loadSubredditInfo() {
        viewModelScope.launch { getSubredditInfoUseCase.loadSubredditInfo(subredditName.value) }
    }

    override fun loadPage() {
        viewModelScope.launch {
            getSubredditPostsUseCase.loadNextPage(
                subredditSorting = subredditPrefs.value.subredditSorting,
                subreddit = subredditName.value
            )
        }
    }

    override fun reload() {
        viewModelScope.launch {
            getSubredditPostsUseCase.loadNextPage(
                reload = true,
                subredditSorting = subredditPrefs.value.subredditSorting,
                subreddit = subredditName.value
            )
        }
    }

    override fun setPostViewType(type: PostViewType) {
        viewModelScope.launch {
            setSubredditViewTypeUseCase.setPostViewType(subredditName.value, type)
        }
    }

    override fun toggleSubredditSubscription() {
        viewModelScope.launch {
            toggleSubscriptionUseCase.toggleSubscription(subredditName = subredditName.value)
        }
    }

    override fun changeSorting(newSubredditSorting: SubredditSorting) {}

    override fun upVote(id: String) {}

    override fun downVote(id: String) {}

    override fun save(id: String) {}
}

data class ViewState(
    val subredditPrefs: SubredditPrefs,
    val feed: PagingModel<List<PostUiModel>>,
    val subredditInfo: SubredditInfoState?,
    val subscriptionState: Boolean?
) {
    constructor() : this(
        subredditPrefs = SubredditPrefs(
            subredditName = Constants.FRONTPAGE,
            viewType = PostViewType.LARGE,
            subredditSorting = Hot
        ),
        feed = PagingModel(content = emptyList(), footer = Loading),
        subredditInfo = SubredditInfoState.Loading,
        subscriptionState = null
    )
}
