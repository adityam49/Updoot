package com.ducktapedapps.updoot.ui.explore

import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.api.local.SubredditDAO
import com.ducktapedapps.updoot.model.Subreddit
import com.ducktapedapps.updoot.ui.explore.trending.TrendingUiModel
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ExperimentalCoroutinesApi
class ExploreRepo @Inject constructor(
        private val redditClient: RedditClient,
        private val subredditDAO: SubredditDAO
) {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val query: MutableStateFlow<String> = MutableStateFlow("")
    val results: Flow<List<Subreddit>> = query.map { queryKeyword ->
        if (queryKeyword.isBlank()) emptyList<Subreddit>()
        else subredditDAO.observeSubredditWithKeyword(queryKeyword)
    }.flowOn(Dispatchers.IO)

    val trendingSubs: Flow<List<ExploreUiModel>> = subredditDAO.observeTrendingSubs().distinctUntilChanged().map {
        if (it.isNotEmpty()) it.mapToTrendingModel()
        else emptyList()
    }

    suspend fun loadTrendingSubs() {
        _isLoading.value = true
        withContext(Dispatchers.IO) {
            try {
                subredditDAO.getTrendingSubs().apply {
                    if (isEmpty()) {
                        fetchNewTrendingSubs()
                    } else if (isStale()) {
                        fetchNewTrendingSubs()
                        forEach { subredditDAO.insertSubreddit(it.copy(isTrending = 0)) }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    @Throws(Exception::class)
    private suspend fun fetchNewTrendingSubs() {
        val api = redditClient.api()
        val subs = api.getTrendingSubredditNames()
        subs.forEach {
            api.getSubredditInfo(it).apply {
                subredditDAO.insertSubreddit(copy(lastUpdated = System.currentTimeMillis(), isTrending = 1))
            }
        }
    }

    private fun List<Subreddit>.isStale(): Boolean = any {
        (System.currentTimeMillis() - (it.lastUpdated
                ?: 0L) > TRENDING_SUBS_STALE_THRESHOLD_IN_HOURS * 60 * 60 * 1000)
    }

    private fun List<Subreddit>.mapToTrendingModel(): List<ExploreUiModel> = listOf(
            HeaderUiModel("Trending today ", R.drawable.ic_baseline_trending_up_24),
            TrendingUiModel(this))

    suspend fun searchSubreddit(queryString: String) {
        _isLoading.value = true
        withContext(Dispatchers.IO) {
            query.value = queryString
            if (queryString.isNotBlank())
                try {
                    val redditAPI = redditClient.api()
                    val results = redditAPI.search(query = queryString)
                    results!!.children.forEach { subreddit -> subredditDAO.insertSubreddit(subreddit) }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                } finally {
                    _isLoading.value = false
                }
        }
    }

    companion object {
        private const val TRENDING_SUBS_STALE_THRESHOLD_IN_HOURS = 12
        private const val TAG = "ExploreRepo"
    }
}