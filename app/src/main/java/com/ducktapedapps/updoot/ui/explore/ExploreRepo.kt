package com.ducktapedapps.updoot.ui.explore

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.api.local.SubredditDAO
import com.ducktapedapps.updoot.model.Subreddit
import com.ducktapedapps.updoot.ui.explore.trending.TrendingUiModel
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ExploreRepo @Inject constructor(
        private val redditClient: RedditClient,
        private val subredditDAO: SubredditDAO
) {

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val query: MutableLiveData<String> = MutableLiveData("")
    val results: LiveData<List<Subreddit>> = Transformations.switchMap(query) { queryString ->
        if (queryString.isNullOrBlank()) MutableLiveData<List<Subreddit>>(emptyList())
        else subredditDAO.observeSubredditWithKeyword(queryString)
    }

    val trendingSubs: LiveData<List<ExploreUiModel>> = Transformations.map(subredditDAO.observeTrendingSubs()) {
        if (it.isEmpty()) listOf()
        else it.mapToTrendingModel()
    }

    suspend fun loadTrendingSubs() {
        _isLoading.postValue(true)
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
                _isLoading.postValue(false)
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
        _isLoading.postValue(true)
        withContext(Dispatchers.IO) {
            query.postValue(queryString)
            if (queryString.isNotBlank())
                try {
                    val redditAPI = redditClient.api()
                    val results = redditAPI.search(query = queryString)
                    results!!.children.forEach { subreddit -> subredditDAO.insertSubreddit(subreddit) }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                } finally {
                    _isLoading.postValue(false)
                }
        }
    }

    companion object {
        private const val TRENDING_SUBS_STALE_THRESHOLD_IN_HOURS = 12
        private const val TAG = "ExploreRepo"
    }
}