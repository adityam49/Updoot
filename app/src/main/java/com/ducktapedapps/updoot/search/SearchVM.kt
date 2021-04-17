package com.ducktapedapps.updoot.search

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.data.local.model.LocalSubreddit
import com.ducktapedapps.updoot.utils.Constants.DEBOUNCE_TIME_OUT
import com.ducktapedapps.updoot.utils.getCompactAge
import com.ducktapedapps.updoot.utils.getCompactCountAsString
import kotlinx.coroutines.flow.*

interface SearchVM {

    val searchQueryLoading: StateFlow<Boolean>

    val includeNsfw: StateFlow<Boolean>

    val results: StateFlow<List<SearchedSubredditResultsUiModel>>

    fun searchSubreddit(queryString: String)

    fun toggleIncludeNsfw()

}

class SearchVMImpl @ViewModelInject constructor(
    private val searchSubredditUseCase: SearchSubredditUseCase,
) : ViewModel(), SearchVM {
    //TODO fix loading indicator
    override val searchQueryLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val query: MutableStateFlow<String> = MutableStateFlow("")

    override val includeNsfw: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override fun searchSubreddit(queryString: String) {
        query.value = queryString
    }

    override fun toggleIncludeNsfw() {
        includeNsfw.value = !includeNsfw.value
    }

    override val results: StateFlow<List<SearchedSubredditResultsUiModel>> = combine(
        query.debounce(DEBOUNCE_TIME_OUT),
        includeNsfw
    ) { keyWork, isNsfw -> searchSubredditUseCase.getSubreddits(keyWork, isNsfw) }
        .flattenMerge()
        .map { subreddits ->
            subreddits.map { subreddit -> subreddit.toSearchedSubredditResultsUiModel() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
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