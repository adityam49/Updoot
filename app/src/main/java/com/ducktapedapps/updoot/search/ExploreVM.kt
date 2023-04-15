package com.ducktapedapps.updoot.search

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.data.local.model.LocalSubreddit
import com.ducktapedapps.updoot.utils.getCompactAge
import com.ducktapedapps.updoot.utils.getCompactCountAsString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
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
    private val results: Flow<List<LocalSubreddit>> = includeNsfw.flatMapLatest {
        searchSubredditUseCase.getSubreddits(query, it, viewModelScope)
    }

    override val viewState: StateFlow<ViewState> = combine(
        query,
        includeNsfw,
        results,
    ) { queryValue, includeNsfwValue, subredditResults ->
        ViewState(
            includeNsfwSearchResults = includeNsfwValue,
            searchQuery = queryValue,
            loading = searchQueryLoading.value,
            subredditSearchResults = subredditResults.map {
                it.toSearchedSubredditResultsUiModel(
                    queryValue
                )
            }
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
    val subredditName: AnnotatedString,
    val icon: String,
    val subscriberCount: String,
    val age: String
)

fun LocalSubreddit.toSearchedSubredditResultsUiModel(queryKeyword: String) =
    SearchedSubredditResultsUiModel(
        subredditName = buildAnnotatedString {
            val tokens = subredditName.split(queryKeyword, ignoreCase = true)
            tokens.forEachIndexed { index, token ->
                append(token)
                if (index != tokens.lastIndex) {
                    pushStyle(SpanStyle(fontWeight = FontWeight.ExtraBold ))
                    append(queryKeyword)
                    pop()
                }
            }
        },
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
