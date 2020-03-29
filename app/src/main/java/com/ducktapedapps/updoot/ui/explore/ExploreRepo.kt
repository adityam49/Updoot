package com.ducktapedapps.updoot.ui.explore

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ducktapedapps.updoot.api.local.SubredditDAO
import com.ducktapedapps.updoot.model.Subreddit
import com.ducktapedapps.updoot.utils.accountManagement.Reddit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "ExploreRepo"

class ExploreRepo @Inject constructor(
        private val reddit: Reddit,
        private val subredditDAO: SubredditDAO
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
                subredditDAO.getTrendingSubreddits().apply {
                    if (this.isNotEmpty()) _trendingSubs.postValue(this)
                    val api = reddit.authenticatedAPI()
                    val trendingSubs = api.getTrendingSubredditNames()
                    if (this.isNotEmpty()) forEach { subredditDAO.insertSubreddit(it.copy(isTrending = 0, lastUpdated = System.currentTimeMillis())) }
                    val fetchedSubs: MutableList<Subreddit> = mutableListOf()
                    for (sub in trendingSubs) {
                        async {
                            api.getSubredditInfo(sub).apply {
                                this.copy(isTrending = 1, lastUpdated = System.currentTimeMillis()).apply { subredditDAO.insertSubreddit(this) }
                                fetchedSubs += this
                                _trendingSubs.postValue(fetchedSubs)
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