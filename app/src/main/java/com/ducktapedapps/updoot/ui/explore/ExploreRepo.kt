package com.ducktapedapps.updoot.ui.explore

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    private val _results: MutableLiveData<List<ExploreUiModel>> = MutableLiveData()
    val results: LiveData<List<ExploreUiModel>> = _results

    private val _trendingSubs: MutableLiveData<List<ExploreUiModel>> = MutableLiveData()
    val trendingSubs: LiveData<List<ExploreUiModel>> = _trendingSubs

    suspend fun loadTrendingSubs() {
        _isLoading.postValue(true)
        withContext(Dispatchers.IO) {
            try {
                subredditDAO.getTrendingSubreddits().apply {
                    if (this.isNotEmpty()) _trendingSubs.postValue(listOf(
                            HeaderUiModel("Trending today ", R.drawable.ic_baseline_trending_up_24),
                            TrendingUiModel(this)
                    ))
                    val api = redditClient.api()
                    val trendingSubs = api.getTrendingSubredditNames()
                    if (this.isNotEmpty()) forEach { subredditDAO.insertSubreddit(it.copy(isTrending = 0, lastUpdated = System.currentTimeMillis())) }
                    val fetchedSubs: MutableList<Subreddit> = mutableListOf()
                    for (sub in trendingSubs) {
                        api.getSubredditInfo(sub).apply {
                            this.copy(isTrending = 1, lastUpdated = System.currentTimeMillis()).apply { subredditDAO.insertSubreddit(this) }
                            fetchedSubs += this
                            _trendingSubs.postValue(listOf(
                                    HeaderUiModel("Trending today ", R.drawable.ic_baseline_trending_up_24),
                                    TrendingUiModel(fetchedSubs))
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    suspend fun searchSubreddit(query: String) {
        _isLoading.postValue(true)
        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val redditAPI = redditClient.api()
                try {
                    val results = redditAPI.search(query = query)
                    if (results != null) {
                        _results.postValue(
                                mutableListOf<ExploreUiModel>().apply {
                                    add(HeaderUiModel("Search results ", R.drawable.ic_search_24dp))
                                    addAll(results.children)
                                }
                        )
                    } else Log.e(this.javaClass.simpleName, "search results from retrofit are null")
                } catch (ex: Exception) {
                    Log.e("ExploreRepo", "Unable to fetch search json ", ex)
                }
            } catch (ex: Exception) {
                Log.e("ExploreRepo", "unable to authenticate api : ", ex)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    companion object {
        private const val TAG = "ExploreRepo"
    }
}