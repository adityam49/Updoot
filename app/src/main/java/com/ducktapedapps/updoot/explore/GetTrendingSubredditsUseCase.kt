package com.ducktapedapps.updoot.explore

import com.ducktapedapps.updoot.data.local.SubredditDAO
import com.ducktapedapps.updoot.data.local.TrendingSubreddit
import com.ducktapedapps.updoot.data.local.model.LocalSubreddit
import com.ducktapedapps.updoot.data.mappers.toLocalSubreddit
import com.ducktapedapps.updoot.data.remote.model.RemoteSubreddit
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transformLatest
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface GetTrendingSubredditsUseCase {

    val trendingSubreddits: Flow<List<LocalSubreddit>>

}

class GetTrendingSubredditsUseCaseImpl @Inject constructor(
    private val redditClient: RedditClient,
    private val subredditDAO: SubredditDAO,
) : GetTrendingSubredditsUseCase {
    override val trendingSubreddits: Flow<List<LocalSubreddit>> = subredditDAO
        .observeTrendingSubreddits()
        .distinctUntilChanged { old, new ->
            val oldSubs = old.map { it.subredditName }.toSet()
            val newSubs = new.map { it.subredditName }.toSet()
            val oldSubsContainNewSubs = oldSubs.containsAll(newSubs)
            val newSubsContainOldSubs = newSubs.containsAll(oldSubs)
            !(oldSubsContainNewSubs && newSubsContainOldSubs)
        }
        .transformLatest { cachedTrendingSubs ->
            if (
                cachedTrendingSubs.isEmpty() ||
                !cachedTrendingSubs.allPresent() ||
                cachedTrendingSubs.areStale()
            ) {
                try {
                    val newSubs = fetchNewTrendingSubs()
                    removeOldTrendingSubreddits()
                    newSubs.saveToCache()
                } catch (e: Exception) {
                    e.printStackTrace()
                    //TODO move exception handling to flow with retry mechanism
                }
            } else emit(cachedTrendingSubs)
        }

    private suspend fun fetchNewTrendingSubs(): List<RemoteSubreddit> {
        val api = redditClient.api()
        val result = api.getTrendingSubredditNames()
        return result.subreddit_names.map { id ->
            api.getSubredditInfo(id)
        }.toList()
    }

    private fun List<LocalSubreddit>.areStale(): Boolean = any {
        val diff = System.currentTimeMillis() - it.lastUpdated.time
        val diffInHours = TimeUnit.HOURS.toHours(diff)
        val threshold = TRENDING_SUBS_STALE_THRESHOLD_IN_HOURS * 60 * 60 * 1000
        diffInHours > threshold
    }

    private fun List<LocalSubreddit>.allPresent() = this.size == 5

    private suspend fun removeOldTrendingSubreddits() {
        subredditDAO.removeAllTrendingSubs()
    }

    private suspend fun List<RemoteSubreddit>.saveToCache() {
        forEach {
            subredditDAO.insertSubreddit(it.toLocalSubreddit().copy(lastUpdated = Date()))
            subredditDAO.insertTrendingSubreddit(TrendingSubreddit(it.display_name))
        }
    }

    companion object {
        private const val TRENDING_SUBS_STALE_THRESHOLD_IN_HOURS = 12
    }
}