package com.ducktapedapps.updoot.ui.explore

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.model.Subreddit
import com.ducktapedapps.updoot.utils.accountManagement.Reddit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ExploreRepo(application: Application) {
    init {
        (application as UpdootApplication).updootComponent.inject(this)
    }

    @Inject
    lateinit var reddit: Reddit

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _results: MutableLiveData<List<Subreddit>> = MutableLiveData()
    val results: LiveData<List<Subreddit>> = _results

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