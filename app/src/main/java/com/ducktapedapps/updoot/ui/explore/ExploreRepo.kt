package com.ducktapedapps.updoot.ui.explore

import com.ducktapedapps.updoot.data.local.SubredditDAO
import com.ducktapedapps.updoot.data.local.model.Subreddit
import com.ducktapedapps.updoot.utils.accountManagement.IRedditClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ExploreRepo @Inject constructor(
        private val redditClient: IRedditClient,
        private val subredditDAO: SubredditDAO
) {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val trendingSubs: Flow<List<Subreddit>> = subredditDAO.observeTrendingSubs().distinctUntilChanged()

    suspend fun loadTrendingSubs() {
        _isLoading.value = true
        withContext(Dispatchers.IO) {
            try {
                subredditDAO.getTrendingSubs().apply {
                    when {
                        isEmpty() -> fetchNewTrendingSubs()
                        isStale() -> {
                            fetchNewTrendingSubs()
                            forEach { subredditDAO.insertSubreddit(it.copy(isTrending = 0)) }
                        }
                        else -> delay(1_000)
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
        val result = api.getTrendingSubredditNames()
        result.subreddit_names.forEach {
            (api.getSubredditInfo(it)).apply {
                subredditDAO.insertSubreddit(copy(lastUpdated = System.currentTimeMillis(), isTrending = 1))
            }
        }
    }

    private fun List<Subreddit>.isStale(): Boolean = any {
        (System.currentTimeMillis() - (it.lastUpdated
                ?: 0L) > TRENDING_SUBS_STALE_THRESHOLD_IN_HOURS * 60 * 60 * 1000)
    }

    companion object {
        private const val TRENDING_SUBS_STALE_THRESHOLD_IN_HOURS = 12
        private const val TAG = "ExploreRepo"
    }
}