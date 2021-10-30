package com.ducktapedapps.updoot.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.data.local.model.LocalSubreddit
import com.ducktapedapps.updoot.utils.getCompactAge
import com.ducktapedapps.updoot.utils.getCompactCountAsString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

interface ExploreVM {

    val viewState: StateFlow<ViewState>

    fun searchSubreddit(queryString: String)

    fun toggleIncludeNsfw()

}

@HiltViewModel
class ExploreVMImpl @Inject constructor(
    private val searchSubredditUseCase: SearchSubredditUseCase,
    private val searchPrefsManager: SearchPrefsManager,
) : ViewModel(), ExploreVM {
    private val searchQueryLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val query: MutableStateFlow<String> = MutableStateFlow("")
    private val includeNsfw: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val results: Flow<List<LocalSubreddit>> =
        combine(query, includeNsfw) { queryValue, isNsfw ->
            searchSubredditUseCase.getSubreddits(
                queryValue,
                isNsfw
            )
        }.flattenMerge()

    override val viewState: StateFlow<ViewState> = combine(
        query,
        includeNsfw,
        results,
    ) { queryValue, includeNsfwValue, subredditResults ->
        ViewState(
            includeNsfwSearchResults = includeNsfwValue,
            searchQuery = queryValue,
            loading = searchQueryLoading.value,
            subredditSearchResults = subredditResults.map { it.toSearchedSubredditResultsUiModel() }
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, ViewState.defaultState())


    override fun searchSubreddit(queryString: String) {
        query.value = queryString
    }

    override fun toggleIncludeNsfw() {
        viewModelScope.launch { searchPrefsManager.toggleNsfwResultsPrefs() }
    }
}

data class SearchedSubredditResultsUiModel(
    val subredditName: String,
    val icon: String,
    val subscriberCount: String,
    val age: String
)

fun LocalSubreddit.toSearchedSubredditResultsUiModel() = SearchedSubredditResultsUiModel(
    subredditName = subredditName,
    icon = icon,
    subscriberCount = subscribers?.run { getCompactCountAsString(this) } ?: "",
    age = getCompactAge(created.time)
)

data class ViewState(
    val includeNsfwSearchResults: Boolean,
    val searchQuery: String,
    val subredditSearchResults: List<SearchedSubredditResultsUiModel>,
    val loading: Boolean,
) {
    companion object {
        fun defaultState() = ViewState(
            includeNsfwSearchResults = false,
            searchQuery = "",
            subredditSearchResults = emptyList(),
            loading = false
        )
    }
}
