package com.ducktapedapps.updoot.ui.search

import com.ducktapedapps.updoot.data.local.SubredditDAO
import com.ducktapedapps.updoot.data.local.model.LocalSubreddit
import com.ducktapedapps.updoot.data.mappers.toLocalSubreddit
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.coroutines.flow.*
import javax.inject.Inject

interface SearchSubredditUseCase {

    suspend fun getSubreddits(
        query: String,
        includeNsfw: Boolean = false
    ): Flow<List<LocalSubreddit>>

}

class SearchSubredditUseCaseImpl @Inject constructor(
    private val subredditDAO: SubredditDAO,
    private val redditClient: RedditClient,
) : SearchSubredditUseCase {

    override suspend fun getSubreddits(
        query: String,
        includeNsfw: Boolean
    ): Flow<List<LocalSubreddit>> = combine(
        getLocalSubreddits(query),
        getRemoteSubreddits(query, includeNsfw),
    ) { localSubs, remoteSubs ->
        localSubs + remoteSubs.filterNot { it.subredditName in localSubs.map { subs -> subs.subredditName } }
    }

    private fun getLocalSubreddits(
        query: String
    ): Flow<List<LocalSubreddit>> =
        if (query.isNotBlank()) subredditDAO
            .observeSubredditWithKeyword(keyword = query.trim())
            .take(2)
            .distinctUntilChanged()
        else
            subredditDAO.observeTrendingSubreddits()

    private fun getRemoteSubreddits(
        query: String,
        includeNsfw: Boolean
    ): Flow<List<LocalSubreddit>> = flow {
        emit(emptyList<LocalSubreddit>())
        if (query.isNotBlank()) try {
            val api = redditClient.api()
            val results = api.search(query = query, includeOver18 = includeNsfw)
            emit(results.children.map { it.toLocalSubreddit() })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}