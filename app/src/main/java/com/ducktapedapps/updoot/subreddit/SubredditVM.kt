package com.ducktapedapps.updoot.subreddit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.navigation.Event.ScreenNavigationEvent
import com.ducktapedapps.navigation.NavigationDirections.SubredditScreenNavigation
import com.ducktapedapps.navigation.NavigationDirections.UserScreenNavigation
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.common.BottomSheetItemType
import com.ducktapedapps.updoot.common.MenuItemModel
import com.ducktapedapps.updoot.data.local.SubredditPrefs
import com.ducktapedapps.updoot.subreddit.OptionsBottomSheetEvent.Empty
import com.ducktapedapps.updoot.subreddit.OptionsBottomSheetEvent.Post
import com.ducktapedapps.updoot.subreddit.OptionsBottomSheetEvent.Subreddit
import com.ducktapedapps.updoot.subreddit.ScreenAction.ChangeSorting
import com.ducktapedapps.updoot.subreddit.ScreenAction.DownVote
import com.ducktapedapps.updoot.subreddit.ScreenAction.LoadPage
import com.ducktapedapps.updoot.subreddit.ScreenAction.LoadSubredditInfo
import com.ducktapedapps.updoot.subreddit.ScreenAction.ReloadPage
import com.ducktapedapps.updoot.subreddit.ScreenAction.SavePost
import com.ducktapedapps.updoot.subreddit.ScreenAction.ShowPostOptions
import com.ducktapedapps.updoot.subreddit.ScreenAction.ShowSubredditOptions
import com.ducktapedapps.updoot.subreddit.ScreenAction.ToggleSubredditPostsViewMode
import com.ducktapedapps.updoot.subreddit.ScreenAction.ToggleSubredditSubscription
import com.ducktapedapps.updoot.subreddit.ScreenAction.UpVote
import com.ducktapedapps.updoot.subreddit.SubredditSorting.Hot
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.PagingModel
import com.ducktapedapps.updoot.utils.PagingModel.Footer.Loading
import com.ducktapedapps.updoot.utils.PostViewType
import com.ducktapedapps.updoot.utils.PostViewType.LARGE
import com.ducktapedapps.updoot.utils.accountManagement.UpdootAccountsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

interface SubredditVM {

    val viewState: StateFlow<ViewState>

    fun doAction(action: ScreenAction)
}

@HiltViewModel
class SubredditVMImpl @Inject constructor(
    getSubredditPreferencesUseCase: GetSubredditPreferencesUseCase,
    private val getSubredditInfoUseCase: GetSubredditInfoUseCase,
    private val getSubredditPostsUseCase: GetSubredditPostsUseCase,
    private val setSubredditViewTypeUseCase: SetSubredditPostViewTypeUseCase,
    private val toggleSubscriptionUseCase: EditSubredditSubscriptionUseCase,
    private val toggleSubredditPostsViewUseCase: EditSubredditPostsViewModeUseCase,
    private val getSubredditSubscriptionState: GetSubredditSubscriptionState,
    private val accountsProvider: UpdootAccountsProvider,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel(), SubredditVM {
    private val subredditName: StateFlow<String> = savedStateHandle.getStateFlow(
        SubredditScreenNavigation.SUBREDDIT_NAME_KEY, ""
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

    private val bottomSheetEventBus = MutableStateFlow<OptionsBottomSheetEvent>(Empty)

    init {
        combine(subredditName, subredditPrefs) { _, _ ->
            loadPage()
        }.launchIn(viewModelScope)
        subredditName
            .map { it == Constants.FRONTPAGE }
            .takeWhile { isFrontPage -> isFrontPage }
            .combine(accountsProvider.getCurrentAccount()) { isFrontPage, currentAccount ->
                if (isFrontPage) {
                    Timber.d("Reloading for account $currentAccount")
                    reload()
                }
            }.launchIn(viewModelScope)
    }

    override val viewState: StateFlow<ViewState> = combine(
        subredditPrefs,
        pagesOfPosts,
        subredditInfo,
        subscriptionState,
        bottomSheetEventBus,
    ) { subredditPrefsValue,
        feed,
        subredditInfoValue,
        subscriptionStateValue,
        bottomSheetEvent ->
        val subredditName = if (subredditPrefsValue.subredditName == Constants.FRONTPAGE) {
            "Frontpage"
        } else {
            subredditPrefsValue.subredditName
        }
        ViewState(
            subredditPrefs = subredditPrefsValue.copy(subredditName = subredditName),
            feed = feed,
            subredditInfo = subredditInfoValue,
            subscriptionState = subscriptionStateValue,
            screenBottomSheetOptions = when (bottomSheetEvent) {
                is Post -> getPostOptions(bottomSheetEvent.post)
                Subreddit -> getSubredditOptions(subredditPrefsValue)
                Empty -> emptyList()
            }
        )
    }.stateIn(
        viewModelScope, SharingStarted.Lazily, ViewState()
    )

    override fun doAction(action: ScreenAction) {
        when (action) {
            LoadPage -> loadPage()
            LoadSubredditInfo -> loadSubredditInfo()
            ReloadPage -> reload()
            ToggleSubredditPostsViewMode -> toggleSubredditPostsViewMode()
            ToggleSubredditSubscription -> toggleSubredditSubscription()
            is DownVote,
            is UpVote,
            is SavePost,
            is ChangeSorting -> Timber.e("Feature not yet supported")

            ShowSubredditOptions -> showSubredditOptions()
            is ShowPostOptions -> showPostOptions(action.post)
        }
    }

    private fun loadSubredditInfo() {
        viewModelScope.launch { getSubredditInfoUseCase.loadSubredditInfo(subredditName.value) }
    }

    private fun loadPage() {
        viewModelScope.launch {
            getSubredditPostsUseCase.loadNextPage(
                subredditSorting = subredditPrefs.value.subredditSorting,
                subreddit = subredditName.value
            )
        }
    }

    private fun reload() {
        viewModelScope.launch {
            getSubredditPostsUseCase.loadNextPage(
                reload = true,
                subredditSorting = subredditPrefs.value.subredditSorting,
                subreddit = subredditName.value
            )
        }
    }

    private fun setPostViewType(type: PostViewType) {
        viewModelScope.launch {
            setSubredditViewTypeUseCase.setPostViewType(subredditName.value, type)
        }
    }

    private fun toggleSubredditSubscription() {
        viewModelScope.launch {
            toggleSubscriptionUseCase.toggleSubscription(subredditName = subredditName.value)
        }
    }

    private fun changeSorting(newSubredditSorting: SubredditSorting) {}

    private fun upVote(id: String) {}

    private fun downVote(id: String) {}

    private fun save(id: String) {}

    private fun toggleSubredditPostsViewMode() {
        viewModelScope.launch {
            toggleSubredditPostsViewUseCase.toggleViewType(subredditName.value)
        }
    }

    private fun showSubredditOptions() {
        bottomSheetEventBus.value = Subreddit
    }

    private fun showPostOptions(post: PostUiModel) {
        bottomSheetEventBus.value = Post(post)
    }

    private fun getSubredditOptions(prefs: SubredditPrefs): List<MenuItemModel> {
        return listOf(
            MenuItemModel(
                onClick = {
                    viewModelScope.launch {
                        toggleSubredditPostsViewUseCase.toggleViewType(prefs.subredditName)
                    }
                    BottomSheetItemType.ViewTypeChange
                },
                title = if (prefs.viewType == LARGE) "Show Compact Posts" else "Show Large Posts",
                icon = if (prefs.viewType == LARGE) R.drawable.ic_list_view_24dp else R.drawable.ic_card_view_24dp
            )
        )
    }

    private fun getPostOptions(post: PostUiModel): List<MenuItemModel> {
        return listOf(
            MenuItemModel(
                onClick = {
                    BottomSheetItemType.ScreenNavigation(
                        ScreenNavigationEvent(
                            SubredditScreenNavigation.open(
                                post.subredditName
                            )
                        )
                    )
                },
                title = post.subredditName,
                icon = R.drawable.ic_subreddit_default_24dp
            ),
            MenuItemModel(
                onClick = {
                    BottomSheetItemType.ScreenNavigation(
                        ScreenNavigationEvent(
                            UserScreenNavigation.open(
                                post.author
                            )
                        )
                    )
                },
                title = post.author,
                icon = R.drawable.ic_account_circle_24dp
            )
        )
    }
}

sealed class ScreenAction {
    object LoadPage : ScreenAction()
    object ReloadPage : ScreenAction()
    object LoadSubredditInfo : ScreenAction()
    object ToggleSubredditSubscription : ScreenAction()
    data class ChangeSorting(val newSubredditSorting: SubredditSorting) : ScreenAction()
    data class UpVote(val id: String) : ScreenAction()
    data class DownVote(val id: String) : ScreenAction()
    data class SavePost(val id: String) : ScreenAction()
    object ToggleSubredditPostsViewMode : ScreenAction()
    object ShowSubredditOptions : ScreenAction()
    data class ShowPostOptions(val post: PostUiModel) : ScreenAction()

}

private sealed class OptionsBottomSheetEvent {
    data class Post(val post: PostUiModel) : OptionsBottomSheetEvent()
    object Subreddit : OptionsBottomSheetEvent()

    object Empty : OptionsBottomSheetEvent()
}

data class ViewState(
    val subredditPrefs: SubredditPrefs = SubredditPrefs(
        subredditName = Constants.FRONTPAGE,
        viewType = LARGE,
        subredditSorting = Hot
    ),
    val feed: PagingModel<List<PostUiModel>> = PagingModel(content = emptyList(), footer = Loading),
    val subredditInfo: SubredditInfoState? = SubredditInfoState.Loading,
    val subscriptionState: Boolean? = null,
    val screenBottomSheetOptions: List<MenuItemModel> = emptyList(),
)
