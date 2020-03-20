package com.ducktapedapps.updoot.ui.subreddit

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ducktapedapps.updoot.api.local.SubredditPrefsDAO
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.model.Subreddit
import com.ducktapedapps.updoot.model.SubredditPrefs
import com.ducktapedapps.updoot.utils.Constants.FRONTPAGE
import com.ducktapedapps.updoot.utils.SingleLiveEvent
import com.ducktapedapps.updoot.utils.Sorting
import com.ducktapedapps.updoot.utils.SubmissionUiType
import com.ducktapedapps.updoot.utils.accountManagement.Reddit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.IOException
import java.util.*
import javax.inject.Inject

class SubmissionRepo @Inject constructor(private val reddit: Reddit, private val dao: SubredditPrefsDAO) {

    var after: String? = null
    private var expandedSubmissionIndex = -1

    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _allSubmissions = MutableLiveData<MutableList<LinkData>>(ArrayList())
    val allSubmissions: LiveData<MutableList<LinkData>> = _allSubmissions

    private val _toastMessage = MutableLiveData(SingleLiveEvent<String?>(null))
    val toastMessage: LiveData<SingleLiveEvent<String?>> = _toastMessage

    private val _subredditInfo: MutableLiveData<Subreddit> = MutableLiveData()
    val subredditInfo: LiveData<Subreddit> = _subredditInfo

    private val _subredditSorting: MutableLiveData<Sorting> = MutableLiveData()
    val sorting: LiveData<Sorting> = _subredditSorting

    private val _submissionsUI: MutableLiveData<SubmissionUiType> = MutableLiveData()
    val submissionsUI: LiveData<SubmissionUiType> = _submissionsUI

    /**
     * Loads subreddit metaData and saves to persistent storage
     */
    suspend fun loadSubredditPrefs(subreddit: String) {
        var data = dao.getSubredditPrefs(subreddit)
        if (data == null) {
            //creating new entry of preferences
            data = SubredditPrefs(subreddit, SubmissionUiType.COMPACT, Sorting.HOT).also {
                dao.insertSubredditPrefs(it)
            }
        }
        _subredditSorting.postValue(data.sorting)
        _submissionsUI.postValue(data.viewType)
    }

    /**
     *  Loads subreddit info like subs, description etc
     */
    suspend fun loadSubredditInfo(subreddit: String) {
        if (subreddit == FRONTPAGE) {
            _subredditInfo.postValue(Subreddit(
                    display_name = "Front page",
                    community_icon = "",
                    public_description = "The front page of the internet",
                    active_user_count = 0L,
                    subscribers = 0L,
                    created = 1137566705,
                    lastUpdated = System.currentTimeMillis() / 1000
            ))
        } else
            try {
                val api = reddit.authenticatedAPI()
                _subredditInfo.postValue(api.getSubredditInfo(subreddit))
            } catch (exception: IOException) {
                exception.printStackTrace()
            }
    }

    suspend fun toggleUI(subreddit: String) {
        val uiType = if (_submissionsUI.value == SubmissionUiType.COMPACT)
            SubmissionUiType.LARGE
        else
            SubmissionUiType.COMPACT
        dao.setUIType(uiType, subreddit)
        _submissionsUI.postValue(uiType)
    }

    suspend fun changeSort(newSort: Sorting, subreddit: String) {
        withContext(Dispatchers.IO) {
            if (_subredditSorting.value != newSort) {
                dao.setSorting(newSort, subreddit)
                _subredditSorting.postValue(newSort)
                loadPage(subreddit, newSort, null, false)
            }
        }
    }

    suspend fun loadPage(subreddit: String, sort: Sorting, time: String?, appendPage: Boolean) {
        withContext(Dispatchers.IO) {
            _isLoading.postValue(true)
            try {
                val redditAPI = reddit.authenticatedAPI()
                try {
                    val submissions: MutableList<LinkData> = if (appendPage) _allSubmissions.value
                            ?: mutableListOf()
                    else {
                        after = null
                        mutableListOf()
                    }
                    val fetchedSubmissions = redditAPI.getSubreddit(subreddit, sort.toString(), time, after)
                    withContext(Dispatchers.Default) {
                        after = fetchedSubmissions.after
                        submissions += fetchedSubmissions.submissions
                        _allSubmissions.postValue(submissions)
                    }
                } catch (e: Exception) {
                    Log.e(this.javaClass.simpleName, "unable to fetch json ", e)
                    _toastMessage.postValue(SingleLiveEvent("Something went wrong! try again later some time"))
                }
            } catch (ex: Exception) {
                Log.e(this.javaClass.simpleName, "unable to get reddit api")
                _toastMessage.postValue(SingleLiveEvent("Something went wrong! try again later some later"))

            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun expandSelfText(index: Int) {
        val updatedList = _allSubmissions.value
        if (updatedList?.get(index) != null) {
            var data = updatedList[index]
            if (index == expandedSubmissionIndex) {
                if (data.selftext != null) {
                    data = data.toggleSelfTextExpansion()
                    updatedList[index] = data
                    if (!data.isSelfTextExpanded) expandedSubmissionIndex = -1
                }
            } else {
                data = data.toggleSelfTextExpansion()
                updatedList[index] = data
                if (expandedSubmissionIndex != -1) {
                    data = updatedList[expandedSubmissionIndex]
                    data = data.toggleSelfTextExpansion()
                    updatedList[expandedSubmissionIndex] = data
                }
                expandedSubmissionIndex = index
            }
        }
        _allSubmissions.value = updatedList
    }

    suspend fun save(index: Int) {
        _allSubmissions.value?.let { cachedSubmissions ->
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                try {
                    val redditAPI = reddit.authenticatedAPI()
                    try {
                        val response = if (!cachedSubmissions[index].saved) {
                            redditAPI.save(cachedSubmissions[index].name)
                        } else {
                            redditAPI.unsave(cachedSubmissions[index].name)
                        }
                        if (response == "{}") {
                            val submission = cachedSubmissions.toMutableList()
                            val updatedSubmission = submission[index].save()
                            submission[index] = updatedSubmission
                            _allSubmissions.postValue(submission)
                            _toastMessage.postValue(SingleLiveEvent("Submission ${if (submission[index].saved) "saved" else "unsaved"}!"))
                        } else throw Exception(response)
                    } catch (e: Exception) {
                        Log.e(this.javaClass.simpleName, "unable to save/unsave ", e)
                    }
                } catch (ex: Exception) {
                    Log.e(this.javaClass.simpleName, "unable to get reddit api", ex)
                    _toastMessage.postValue(SingleLiveEvent("Unable to ${if (cachedSubmissions[index].saved) "unsave" else "save"}! Try again later"))
                } finally {
                    _isLoading.postValue(false)
                }

            }
        }
    }

    suspend fun castVote(index: Int, direction: Int) {
        _allSubmissions.value?.let { cachedSubmissions ->
            val submissions = cachedSubmissions.toMutableList()
            if (cachedSubmissions[index].locked) {
                _toastMessage.postValue(SingleLiveEvent("Submission is locked!"))
                return
            }
            if (cachedSubmissions[index].archived) {
                _toastMessage.postValue(SingleLiveEvent("Submission is archived!"))
                return
            }
            withContext(Dispatchers.IO) {
                try {
                    val redditAPI = reddit.authenticatedAPI()
                    try {
                        val intendedDirection = when (direction) {
                            1 -> if (cachedSubmissions[index].likes != true) 1 else 0
                            -1 -> if (cachedSubmissions[index].likes != false) -1 else 0
                            else -> direction
                        }
                        val response = redditAPI.castVote(cachedSubmissions[index].name, intendedDirection)
                        if (response == "{}") {
                            val updatedSubmission = cachedSubmissions[index].vote(direction)
                            submissions[index] = updatedSubmission
                            _allSubmissions.postValue(submissions)
                        } else {
                            throw Exception("unable to vote : $response")
                        }
                    } catch (e: Exception) {
                        Log.e(this.javaClass.simpleName, "unable to vote", e)
                    }
                } catch (ex: Exception) {
                    _toastMessage.postValue(SingleLiveEvent("Unable to vote!"))
                    Log.e(this.javaClass.simpleName, "unable to get reddit api", ex)
                }
            }
        }
    }
}

