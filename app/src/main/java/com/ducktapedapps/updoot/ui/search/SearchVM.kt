package com.ducktapedapps.updoot.ui.search

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.data.local.SubredditDAO
import com.ducktapedapps.updoot.data.local.model.Subreddit
import com.ducktapedapps.updoot.utils.Constants.DEBOUNCE_TIME_OUT
import com.ducktapedapps.updoot.utils.accountManagement.IRedditClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class SearchVM @ViewModelInject constructor(
        private val subredditDAO: SubredditDAO,
        private val redditClient: IRedditClient,
) : ViewModel() {
    private val _searchQueryLoading = MutableStateFlow(false)
    val searchQueryLoading: StateFlow<Boolean> = _searchQueryLoading

    private val query: MutableStateFlow<String> = MutableStateFlow("")

    private val _includeOver18: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val includeOver18: StateFlow<Boolean> = _includeOver18

    private val remoteResults: Flow<List<Subreddit>> = query
            .debounce(DEBOUNCE_TIME_OUT)
            .mapLatest { keyWord ->
                if (keyWord.isEmpty()) emptyList()
                else getSubredditsWithKeywords(keyWord.trim())
            }
            .catch { throwable ->
                throwable.printStackTrace()
                _searchQueryLoading.value = false
            }

    //TODO : currentUser flow required to injected, refactor required :/
//    private val localResults: Flow<List<Subreddit>> =
//            combine(currentUser, query.debounce(DEBOUNCE_TIME_OUT)) { user: User, keyWord: String ->
//                subredditDAO
//                        .observeSubscribedSubredditsFor(user.name)
//                        .distinctUntilChanged()
//                        .flowOn(Dispatchers.IO)
//                        .map {
//                            it.filter { subreddit ->
//                                subreddit.display_name.contains(keyWord)
//                            }
//                        }
//            }.flattenMerge()

    private suspend fun getSubredditsWithKeywords(keyword: String): List<Subreddit> = withContext(Dispatchers.IO) {
        _searchQueryLoading.value = true
        val redditAPI = redditClient.api()
        val results = redditAPI.search(query = keyword)
        _searchQueryLoading.value = false
        results.children
    }

    fun searchSubreddit(queryString: String) {
        query.value = queryString
    }

    fun toggleIncludeOver18() {
        _includeOver18.value = !includeOver18.value
    }

    val results: StateFlow<List<Subreddit>> = remoteResults.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
//            combine(localResults, remoteResults) { localSubs: List<Subreddit>, remoteSubs: List<Subreddit> ->
//                localSubs + remoteSubs.filterNot { remoteSub ->
//                    localSubs.any { localSub ->
//                        localSub.display_name == remoteSub.display_name
//                    }
//                }
//            }
}