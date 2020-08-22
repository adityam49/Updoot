package com.ducktapedapps.updoot.ui.navDrawer.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.api.local.SubredditDAO
import com.ducktapedapps.updoot.model.Subreddit
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class SubscriptionViewModel @Inject constructor(private val subredditDAO: SubredditDAO, private val redditClient: RedditClient) : ViewModel() {
    private val currentUser: MutableStateFlow<String> = MutableStateFlow(Constants.ANON_USER)
    private val query: MutableStateFlow<String> = MutableStateFlow("")
    private var currentSearchJob: Job? = null

    val results: Flow<List<Subreddit>> = query.combine(currentUser) { keyWord: String, user: String ->
        if (keyWord.isNotBlank()) subredditDAO.observeSubredditWithKeyword(keyWord).distinctUntilChanged()
        else subredditDAO.observeSubscribedSubredditsFor(user).distinctUntilChanged()
    }.flattenMerge()

    fun searchSubreddit(queryString: String) {
        currentSearchJob?.cancel()
        currentSearchJob = viewModelScope.launch(Dispatchers.IO) {
            query.value = queryString
            if (queryString.isNotBlank())
                try {
                    val redditAPI = redditClient.api()
                    val results = redditAPI.search(query = queryString)
                    results!!.children.forEach { subreddit -> subredditDAO.insertSubreddit(subreddit) }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
        }
    }

    fun setCurrentUser(newUser: String) {
        currentUser.value = newUser
    }
}

@FlowPreview
@ExperimentalCoroutinesApi
class SubscriptionViewModelFactory @Inject constructor(
        private val subredditDAO: SubredditDAO,
        private val redditClient: RedditClient
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = SubscriptionViewModel(subredditDAO, redditClient) as T
}