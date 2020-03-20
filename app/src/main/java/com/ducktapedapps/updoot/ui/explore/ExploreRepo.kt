package com.ducktapedapps.updoot.ui.explore

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ducktapedapps.updoot.model.Subreddit
import com.ducktapedapps.updoot.utils.accountManagement.Reddit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.abs

private const val TAG = "ExploreRepo"

class ExploreRepo @Inject constructor(private val reddit: Reddit, private val subredditDAO: SubredditDAO) {

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _results: MutableLiveData<List<Subreddit>> = MutableLiveData()
    val results: LiveData<List<Subreddit>> = _results

    private val _trendingSubs: MutableLiveData<List<Subreddit>> = MutableLiveData()
    val trendingSubs: LiveData<List<Subreddit>> = _trendingSubs

    suspend fun loadTrendingSubs() {
        _isLoading.postValue(true)
        withContext(Dispatchers.IO) {
            try {

                //looking if there are cached trending subreddits
                subredditDAO.getTrendingSubreddits().apply {
                    if (this.isNotEmpty() && !areTrendingSubsStale(this)) _trendingSubs.postValue(this)

                    //no trending subreddits found or subs are stale
                    else {
                        val api = reddit.authenticatedAPI()
                        val trendingSubs = api.getTrendingSubredditNames()
                        val fetchedSubs: MutableList<Subreddit> = _trendingSubs.value?.toMutableList()
                                ?: mutableListOf()
                        for (sub in trendingSubs) {
                            async {
                                api.getSubredditInfo("r/$sub").apply {
                                    subredditDAO.insertSubreddit(this.copy(
                                            isTrending = 1,
                                            lastUpdated = System.currentTimeMillis() / 1000
                                    ))
                                    fetchedSubs += this
                                    _trendingSubs.postValue(fetchedSubs)
                                }
                            }
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

    /**
     * Helper function to determine if cached subs are stale (more than a day old)
     */
    private fun areTrendingSubsStale(trendingSubs: List<Subreddit>): Boolean {
        for (sub in trendingSubs)
            if (abs((sub.lastUpdated ?: 0) - System.currentTimeMillis() / 1000) > 86400000)
                return true
        return false
    }

    suspend fun searchSubreddit(query: String) {
        _isLoading.postValue(true)
        withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val redditAPI = reddit.authenticatedAPI()
                try {
                    val results = redditAPI.search(query = query)
                    if (results != null) {
                        _results.postValue(results.children)
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

}