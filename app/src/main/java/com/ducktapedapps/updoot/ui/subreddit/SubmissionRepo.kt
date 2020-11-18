package com.ducktapedapps.updoot.ui.subreddit

import android.text.TextUtils
import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import com.ducktapedapps.updoot.data.local.SubmissionsCacheDAO
import com.ducktapedapps.updoot.data.local.SubredditDAO
import com.ducktapedapps.updoot.data.local.SubredditPrefs
import com.ducktapedapps.updoot.data.local.SubredditPrefsDAO
import com.ducktapedapps.updoot.data.local.model.LinkData
import com.ducktapedapps.updoot.data.local.model.Subreddit
import com.ducktapedapps.updoot.ui.subreddit.SubredditSorting.*
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.Constants.FRONTPAGE
import com.ducktapedapps.updoot.utils.SingleLiveEvent
import com.ducktapedapps.updoot.utils.SubmissionUiType
import com.ducktapedapps.updoot.utils.accountManagement.IRedditClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "SubmissionRepo"

class SubmissionRepo @Inject constructor(
        private val redditClient: IRedditClient,
        private val prefsDAO: SubredditPrefsDAO,
        private val submissionsCacheDAO: SubmissionsCacheDAO,
        private val subredditDAO: SubredditDAO
) {
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _toastMessage = MutableStateFlow(SingleLiveEvent<String?>(null))
    val toastMessage: StateFlow<SingleLiveEvent<String?>> = _toastMessage

    fun postViewType(subredditName: String): Flow<SubmissionUiType> = prefsDAO.observeViewType(subredditName)
    private val nextPageKeyAndCurrentPageEntries = MutableStateFlow<Map<String?, List<String>>>(mapOf())
    val allSubmissions: Flow<List<LinkData>> = nextPageKeyAndCurrentPageEntries.flatMapLatest {
        if (it.keys.isNotEmpty())
            submissionsCacheDAO.observeCachedSubmissions(cachedSubmissionsObserveQueryInOrderOfGivenIds(it.values.flatten())).distinctUntilChanged()
        else flow { emit(emptyList<LinkData>()) }
    }

    fun subredditInfo(subredditName: String): Flow<Subreddit?> = subredditDAO.observeSubredditInfo(subredditName)

    suspend fun loadAndSaveSubredditInfo(subreddit: String) {
        if (subreddit != FRONTPAGE)
            withContext(Dispatchers.IO) {
                try {
                    val api = redditClient.api()
                    val result = api.getSubredditInfo(subreddit)
                    subredditDAO.insertSubreddit(result)
                } catch (e: Exception) {
                    e.printStackTrace()
                    _toastMessage.value = SingleLiveEvent("Unable to fetch $subreddit metadata : ${e.message}")
                }
            }
    }

    private fun cachedSubmissionsObserveQueryInOrderOfGivenIds(ids: List<String>): SimpleSQLiteQuery =
            SimpleSQLiteQuery(
                    StringBuilder().apply {
                        append(" SELECT * FROM Linkdata")
                        append(" WHERE name IN (")
                        append(TextUtils.join(",", ids.map { "\'$it\'" }))
                        append(")")
                        append(" ORDER BY CASE name ")
                        ids.forEachIndexed { index, id -> append(" WHEN \'$id\' THEN $index ") }
                        append(" END")
                    }.toString(), null)

    fun hasNextPage(): Boolean = nextPageKeyAndCurrentPageEntries.value.keys.last() != null

    suspend fun setPostViewType(subreddit: String, type: SubmissionUiType) = withContext(Dispatchers.IO) {
        prefsDAO.setUIType(type, subreddit)
    }

    suspend fun changeSort(subreddit: String, newSort: SubredditSorting) {
        clearSubmissions()
        withContext(Dispatchers.IO) {
            prefsDAO.setSorting(newSort, subreddit)
            loadPage(subreddit)
        }
    }

    suspend fun vote(id: String, direction: Int) {
        withContext(Dispatchers.IO) {
            try {
                val cachedSubmission = submissionsCacheDAO.getLinkData(id)
                if (cachedSubmission.archived)
                    _toastMessage.value = SingleLiveEvent("Can't vote on archived submission")
                else {
                    val api = redditClient.api()
                    val updatedSubmission = cachedSubmission.withUpdatedVote(direction)
                    val result = api.castVote(cachedSubmission.name, when (updatedSubmission.likes) {
                        true -> 1
                        false -> -1
                        null -> 0
                    })
                    when (result.code()) {
                        200 -> submissionsCacheDAO.insertSubmissions(updatedSubmission)
                        429 -> _toastMessage.value = SingleLiveEvent("Too many vote requests!")
                        else -> Log.e(TAG, result.message())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun LinkData.withUpdatedVote(direction: Int): LinkData {
        var updatedLikes: Boolean? = likes
        var updatedUps = ups
        when (direction) {
            1 -> if (likes == null) {
                updatedLikes = true
                updatedUps++
            } else if (!likes) {
                updatedLikes = true
                updatedUps += 2
            } else {
                updatedLikes = null
                updatedUps--
            }
            -1 -> when {
                likes == null -> {
                    updatedUps--
                    updatedLikes = false
                }
                likes -> {
                    updatedUps -= 2
                    updatedLikes = false
                }
                else -> {
                    updatedUps++
                    updatedLikes = null
                }
            }
        }
        return copy(ups = updatedUps, likes = updatedLikes)
    }

    suspend fun save(id: String) {
        withContext(Dispatchers.IO) {
            try {
                val api = redditClient.api()
                val cachedSubmission = submissionsCacheDAO.getLinkData(id)
                val result = api.run { if (cachedSubmission.saved) unSave(cachedSubmission.name) else save(cachedSubmission.name) }
                when (result.code()) {
                    200 -> submissionsCacheDAO.insertSubmissions(cachedSubmission.copy(saved = !cachedSubmission.saved))
                    else -> Log.e(TAG, result.message())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearSubmissions() {
        nextPageKeyAndCurrentPageEntries.value = emptyMap()
    }

    suspend fun loadPage(subreddit: String) {
        withContext(Dispatchers.IO) {
            try {
                _isLoading.value = true
                loadPageAndCacheToDbSuccessfully(subreddit)
            } catch (e: Exception) {
                e.printStackTrace()
                _toastMessage.value = SingleLiveEvent("Something went wrong! try again later some later")
            } finally {
                _isLoading.value = false
            }
        }
    }

    @Throws(Exception::class)
    private suspend fun loadPageAndCacheToDbSuccessfully(subreddit: String) {
        val redditAPI = redditClient.api()
        val sorting = (prefsDAO.getSubredditSorting(subreddit)
                ?: createAndSaveDefaultSubredditPrefs(subreddit).subredditSorting).mapSorting()
        val fetchedData = redditAPI.getSubreddit(
                "${if (subreddit != FRONTPAGE) "r/" else ""}$subreddit",
                sorting.first,
                sorting.second,
                nextPageKeyAndCurrentPageEntries.value.keys.lastOrNull()
        )
        fetchedData.children.forEach { submission -> submissionsCacheDAO.insertSubmissions(submission) }
        if (fetchedData.children.isNotEmpty()) {
            nextPageKeyAndCurrentPageEntries.value = nextPageKeyAndCurrentPageEntries.value
                    .toMutableMap()
                    .apply { put(fetchedData.after, fetchedData.children.map { linkData -> linkData.name }) }
        }
    }

    private suspend fun createAndSaveDefaultSubredditPrefs(subredditName: String) =
            SubredditPrefs(subredditName, SubmissionUiType.COMPACT, Hot).apply { prefsDAO.insertSubredditPrefs(this) }

    private fun SubredditSorting.mapSorting(): Pair<String, String?> = when (this) {
        Rising -> Pair(Constants.RISING, null)
        Best -> Pair(Constants.BEST, null)
        New -> Pair(Constants.NEW, null)
        Hot -> Pair(Constants.HOT, null)

        TopHour -> Pair(Constants.TOP, Constants.NOW)
        TopDay -> Pair(Constants.TOP, Constants.TODAY)
        TopWeek -> Pair(Constants.TOP, Constants.THIS_WEEK)
        TopMonth -> Pair(Constants.TOP, Constants.THIS_MONTH)
        TopYear -> Pair(Constants.TOP, Constants.THIS_YEAR)
        TopAll -> Pair(Constants.TOP, Constants.ALL_TIME)

        ControversialHour -> Pair(Constants.CONTROVERSIAL, Constants.NOW)
        ControversialDay -> Pair(Constants.CONTROVERSIAL, Constants.TODAY)
        ControversialWeek -> Pair(Constants.CONTROVERSIAL, Constants.THIS_WEEK)
        ControversialMonth -> Pair(Constants.CONTROVERSIAL, Constants.THIS_MONTH)
        ControversialYear -> Pair(Constants.CONTROVERSIAL, Constants.THIS_YEAR)
        ControversialAll -> Pair(Constants.CONTROVERSIAL, Constants.ALL_TIME)
    }
}