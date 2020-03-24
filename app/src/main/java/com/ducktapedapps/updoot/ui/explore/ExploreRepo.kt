package com.ducktapedapps.updoot.ui.explore

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ducktapedapps.updoot.api.local.SubredditDAO
import com.ducktapedapps.updoot.model.Subreddit
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.accountManagement.Reddit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "ExploreRepo"

class ExploreRepo @Inject constructor(
        private val reddit: Reddit,
        private val subredditDAO: SubredditDAO,
        private val sharedPreferences: SharedPreferences
) {

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
                    if (this.isNotEmpty()) _trendingSubs.postValue(this)

                    if (this.isNotEmpty() && !areTrendingSubsStale()) return@apply
                    //getting new trending subs and caching them
                    else {
                        val api = reddit.authenticatedAPI()
                        val trendingSubs = api.getTrendingSubredditNames()
                        sharedPreferences.edit().putLong(Constants.LAST_TRENDING_UPDATED_KEY, System.currentTimeMillis()).apply()
                        val fetchedSubs: MutableList<Subreddit> = _trendingSubs.value?.toMutableList()
                                ?: mutableListOf()
                        for (sub in trendingSubs) {
                            async {
                                api.getSubredditInfo(sub).apply {
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
     * Helper function to determine if cached subs are stale (more than a 12 hrs old)
     */
    private fun areTrendingSubsStale(): Boolean {
        val current = System.currentTimeMillis()
        val last = sharedPreferences.getLong(Constants.LAST_TRENDING_UPDATED_KEY, 0)
        val duration = 12 * 60 * 60 * 1000L
        return current - last > duration
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