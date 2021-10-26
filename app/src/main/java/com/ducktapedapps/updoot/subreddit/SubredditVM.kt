package com.ducktapedapps.updoot.subreddit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.data.local.SubredditPrefs
import com.ducktapedapps.updoot.subreddit.SubredditSorting.*
import com.ducktapedapps.updoot.utils.Constants.FRONTPAGE
import com.ducktapedapps.updoot.utils.PagingModel
import com.ducktapedapps.updoot.utils.PagingModel.Footer.Loading
import com.ducktapedapps.updoot.utils.PostViewType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

interface SubredditVM {

    val viewState: StateFlow<ViewState>

    fun setSubredditName(name: String = "")

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
) : ViewModel(), SubredditVM {
    private val subredditName: MutableStateFlow<String> = MutableStateFlow("")

    private val subredditPrefs: StateFlow<SubredditPrefs> = subredditName
        .flatMapLatest { name ->
            getSubredditPreferencesUseCase.getSubredditPrefsFlow(name)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SubredditPrefs("", viewType = PostViewType.LARGE, subredditSorting = Hot)
        )

    private val pagesOfPosts: StateFlow<PagingModel<List<PostUiModel>>> =
        getSubredditPostsUseCase
            .pagingModel
            .map { model ->
                PagingModel(
                    model.content.map { post -> post.toUiModel() },
                    model.footer
                )
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, PagingModel(emptyList(), Loading))

    private val subredditInfo: StateFlow<SubredditInfoState?> =
        getSubredditInfoUseCase.subredditInfo

    private val subscriptionState: StateFlow<Boolean?> =
        subredditName
            .flatMapLatest { name ->
                getSubredditSubscriptionState
                    .getIsSubredditSubscribedState(name)
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
            }.stateIn(viewModelScope, SharingStarted.Lazily, null)


    override val viewState: StateFlow<ViewState> = combine(
        subredditName, subredditPrefs, pagesOfPosts, subredditInfo, subscriptionState
    ) { subredditNameValue, subredditPrefsValue, feed, subredditInfoValue, subscriptionStateValue ->
        ViewState(
            subredditName = if (subredditNameValue == FRONTPAGE) "Frontpage" else subredditNameValue,
            subredditPrefs = subredditPrefsValue,
            feed = feed,
            subredditInfo = subredditInfoValue,
            subscriptionState = subscriptionStateValue
        )
    }.stateIn(
        viewModelScope, SharingStarted.Lazily, ViewState.defaultState("")
    )

    override fun loadSubredditInfo() {
        viewModelScope.launch { getSubredditInfoUseCase.loadSubredditInfo(subredditName.value) }
    }

    override fun setSubredditName(name: String) {
        subredditName.value = name
        reload()
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
    val subredditName: String,
    val subredditPrefs: SubredditPrefs,
    val feed: PagingModel<List<PostUiModel>>,
    val subredditInfo: SubredditInfoState?,
    val subscriptionState: Boolean?
) {
    companion object {
        fun defaultState(subredditName: String) = ViewState(
            subredditName = "",
            subredditPrefs = SubredditPrefs(
                subredditName = subredditName,
                viewType = PostViewType.LARGE,
                subredditSorting = Hot
            ),
            feed = PagingModel(content = emptyList(), footer = Loading),
            subredditInfo = SubredditInfoState.Loading,
            subscriptionState = null
        )
    }
}
