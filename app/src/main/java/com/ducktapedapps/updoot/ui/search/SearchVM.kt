package com.ducktapedapps.updoot.ui.search

import com.ducktapedapps.updoot.data.local.SubredditDAO
import com.ducktapedapps.updoot.data.local.model.Subreddit
import com.ducktapedapps.updoot.ui.User
import com.ducktapedapps.updoot.utils.accountManagement.IRedditClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
class SearchVM(
        private val subredditDAO: SubredditDAO,
        private val redditClient: IRedditClient,
        private val coroutineScope: CoroutineScope,
        currentUser: Flow<User>
) {
    private val query: MutableStateFlow<String> = MutableStateFlow("")
    private val _searchQueryLoading = MutableStateFlow(false)
    val searchQueryLoading: StateFlow<Boolean> = _searchQueryLoading
    private var currentSearchJob: Job? = null

    val results: Flow<List<Subreddit>> = query.combineTransform(currentUser) { keyWord: String, user: User ->
        if (keyWord.isNotBlank()) emit(subredditDAO.observeSubredditWithKeyword(keyWord).distinctUntilChanged())
        else emit(subredditDAO.observeSubscribedSubredditsFor(user.name).distinctUntilChanged())
    }.flattenMerge()

    fun searchSubreddit(queryString: String) {
        currentSearchJob?.cancel()
        currentSearchJob = coroutineScope.launch(Dispatchers.IO) {
            query.value = queryString
            if (queryString.isNotBlank())
                try {
                    _searchQueryLoading.value = true
                    val redditAPI = redditClient.api()
                    val results = redditAPI.search(query = queryString)
                    results.children.forEach { subreddit -> subredditDAO.insertSubreddit(subreddit) }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                } finally {
                    _searchQueryLoading.value = false
                }
        }
    }
}