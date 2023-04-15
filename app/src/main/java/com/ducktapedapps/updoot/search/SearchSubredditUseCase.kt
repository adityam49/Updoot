package com.ducktapedapps.updoot.search

import com.ducktapedapps.updoot.data.local.SubredditDAO
import com.ducktapedapps.updoot.data.local.model.LocalSubreddit
import com.ducktapedapps.updoot.data.mappers.toLocalSubreddit
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

interface SearchSubredditUseCase {

    fun getSubreddits(
        query: Flow<String>,
        includeNsfw: Boolean = false,
        coroutineScope: CoroutineScope,
    ): Flow<List<LocalSubreddit>>
}

class SearchSubredditUseCaseImpl @Inject constructor(
    private val subredditDAO: SubredditDAO,
    private val redditClient: RedditClient,
) : SearchSubredditUseCase {

    override fun getSubreddits(
        query: Flow<String>,
        includeNsfw: Boolean,
        coroutineScope: CoroutineScope,
    ): Flow<List<LocalSubreddit>> {
        return query
            .debounce(Constants.DEBOUNCE_TIME_OUT)
            .distinctUntilChanged()
            .flatMapLatest {
                coroutineScope.launch {
                    queryRemoteSubreddits(it, includeNsfw)
                }
                getLocalSubreddits(it)
            }
    }

    private fun getLocalSubreddits(
        query: String
    ): Flow<List<LocalSubreddit>> =
        if (query.isNotBlank()) subredditDAO
            .observeSubredditWithKeyword(keyword = query.trim())
            .distinctUntilChanged()
        else flowOf(emptyList())

    private suspend fun queryRemoteSubreddits(
        query: String,
        includeNsfw: Boolean
    ) {
        if (query.isNotBlank()) try {
            Timber.d("Making API call on query $query")
            val api = redditClient.api()
            val results = api.search(query = query, includeOver18 = includeNsfw)
            subredditDAO.insertSubreddits(results.children.map { it.toLocalSubreddit() })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}