package com.ducktapedapps.updoot.ui.explore

import android.util.Log
import com.ducktapedapps.updoot.data.local.SubredditDAO
import com.ducktapedapps.updoot.data.local.TrendingSubreddit
import com.ducktapedapps.updoot.data.local.model.Subreddit
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ExploreRepo @Inject constructor(
        private val redditClient: RedditClient,
        private val subredditDAO: SubredditDAO
) {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val trendingSubs: Flow<List<Subreddit>> = subredditDAO.observeTrendingSubreddits()

    suspend fun loadTrendingSubs() {
        _isLoading.value = true
        withContext(Dispatchers.IO) {
            try {
                val subs = subredditDAO.observeTrendingSubreddits().first()
                if (subs.isEmpty() || subs.isStale()) {
                    val newSubs = fetchNewTrendingSubs()
                    removeOldTrendingSubreddits()
                    newSubs.saveToCache()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    @Throws(Exception::class)
    private suspend fun fetchNewTrendingSubs(): List<Subreddit> {
        val api = redditClient.api()
        val result = api.getTrendingSubredditNames()
        return result.subreddit_names.map { id ->
            api.getSubredditInfo(id)
        }.toList()
    }

    private suspend fun List<Subreddit>.saveToCache() {
        forEach {
            subredditDAO.insertSubreddit(it.copy(lastUpdated = System.currentTimeMillis()))
            subredditDAO.insertTrendingSubreddit(TrendingSubreddit(it.display_name))
        }
    }

    private fun List<Subreddit>.isStale(): Boolean = any {
        val diff = System.currentTimeMillis() - (it.lastUpdated ?: 0L)
        val threshold = TRENDING_SUBS_STALE_THRESHOLD_IN_HOURS * 60 * 60 * 1000
        Log.i(TAG, "diff = $diff and threshold = $threshold")
        diff > threshold
    }

    private suspend fun removeOldTrendingSubreddits() {
        subredditDAO.removeAllTrendingSubs()
    }

    companion object {
        private const val TRENDING_SUBS_STALE_THRESHOLD_IN_HOURS = 12
        private const val TAG = "ExploreRepo"
    }
}