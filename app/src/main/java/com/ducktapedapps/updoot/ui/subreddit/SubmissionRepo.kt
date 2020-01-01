package com.ducktapedapps.updoot.ui.subreddit

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.model.ListingData
import com.ducktapedapps.updoot.model.Thing
import com.ducktapedapps.updoot.utils.SingleLiveEvent
import com.ducktapedapps.updoot.utils.accountManagement.Reddit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class SubmissionRepo(application: Application) {
    init {
        (application as UpdootApplication).updootComponent.inject(this)
    }

    @Inject
    lateinit var reddit: Reddit

    var after: String? = null
    private var expandedSubmissionIndex = -1

    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _allSubmissions = MutableLiveData<MutableList<LinkData>>(ArrayList())
    val allSubmissions: LiveData<MutableList<LinkData>> = _allSubmissions

    private val _toastMessage = MutableLiveData(SingleLiveEvent<String?>(null))
    val toastMessage: LiveData<SingleLiveEvent<String?>> = _toastMessage

    suspend fun loadPage(subreddit: String?, sort: String?, time: String?, appendPage: Boolean) {
        withContext(Dispatchers.IO) {
            _isLoading.postValue(true)

            try {
                val redditAPI = reddit.authenticatedAPI()
                try {
                    if (redditAPI != null) {
                        val thing: Thing = redditAPI.getSubreddit(subreddit, "top", time, after)
                        val submissions = if (appendPage) _allSubmissions.value
                                ?: mutableListOf() else mutableListOf()
                        withContext(Dispatchers.Default) {
                            if (thing.data is ListingData) {
                                for (child in thing.data.children) submissions.add(child.data as LinkData)
                                after = thing.data.after
                                _allSubmissions.postValue(submissions)
                            } else throw Exception("unsupported json response")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(this.javaClass.simpleName, "unable to fetch json ", e)
                    _toastMessage.postValue(SingleLiveEvent("Something went wrong! Try again after some time"))
                }
            } catch (ex: Exception) {
                Log.e(this.javaClass.simpleName, "unable to get reddit api")
                _toastMessage.postValue(SingleLiveEvent("Something went wrong! Try again after some time"))

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
                    if (redditAPI != null) {
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
                    if (redditAPI != null) {
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
                    } else throw Exception("Unable to get authenticated reddit api")
                } catch (ex: Exception) {
                    _toastMessage.postValue(SingleLiveEvent("Unable to vote!"))
                    Log.e(this.javaClass.simpleName, "unable to get reddit api", ex)
                }            }
        }
    }
}

