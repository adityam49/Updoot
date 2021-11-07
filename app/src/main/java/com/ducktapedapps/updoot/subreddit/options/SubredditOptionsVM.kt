package com.ducktapedapps.updoot.subreddit.options

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.data.local.SubredditPrefs
import com.ducktapedapps.updoot.data.local.SubredditPrefsDAO
import com.ducktapedapps.updoot.subreddit.SubredditSorting
import com.ducktapedapps.updoot.subreddit.SubredditSorting.Hot
import com.ducktapedapps.updoot.utils.Constants.FRONTPAGE
import com.ducktapedapps.updoot.utils.PostViewType
import com.ducktapedapps.updoot.utils.PostViewType.COMPACT
import com.ducktapedapps.updoot.utils.PostViewType.LARGE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

interface SubredditOptionsVM {
    val viewState: StateFlow<ViewState>

    fun setSubredditName(name: String)

    fun togglePostViewType()

    fun setSubredditSorting(sorting: SubredditSorting)
}

@HiltViewModel
class SubredditOptionsVMImpl @Inject constructor(
    private val subredditPrefsDAO: SubredditPrefsDAO
) : ViewModel(),
    SubredditOptionsVM {
    private val subredditName: MutableStateFlow<String> = MutableStateFlow(FRONTPAGE)

    override fun setSubredditName(name: String) {
        subredditName.value = name
    }

    override val viewState: StateFlow<ViewState> = subredditName
        .flatMapLatest { subredditPrefsDAO.observeSubredditPrefs(it) }
        .map {
            it?.run { ViewState(it.viewType, it.subredditSorting) } ?: ViewState.defaultState()
        }.stateIn(viewModelScope, SharingStarted.Lazily, ViewState.defaultState())


    override fun togglePostViewType() {
        viewModelScope.launch {
            subredditPrefsDAO.insertSubredditPrefs(
                SubredditPrefs(
                    subredditName = subredditName.value,
                    viewType = if (viewState.value.postType == COMPACT) LARGE else COMPACT,
                    subredditSorting = viewState.value.subredditSorting,
                )
            )
        }
    }

    override fun setSubredditSorting(sorting: SubredditSorting) {
        viewModelScope.launch {
            subredditPrefsDAO.insertSubredditPrefs(
                SubredditPrefs(
                    subredditName = subredditName.value,
                    viewType = viewState.value.postType,
                    subredditSorting = sorting,
                )
            )
        }
    }

}

data class ViewState(
    val postType: PostViewType,
    val subredditSorting: SubredditSorting,
) {
    companion object {
        fun defaultState() = ViewState(LARGE, Hot)
    }
}