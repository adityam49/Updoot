package com.ducktapedapps.updoot.ui.subreddit

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.sqlite.db.SimpleSQLiteQuery
import com.ducktapedapps.updoot.api.local.SubmissionsCacheDAO
import com.ducktapedapps.updoot.api.local.SubredditPrefsDAO
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.model.SubredditPrefs
import com.ducktapedapps.updoot.ui.subreddit.SubredditSorting.*
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.Constants.FRONTPAGE
import com.ducktapedapps.updoot.utils.SingleLiveEvent
import com.ducktapedapps.updoot.utils.SubmissionUiType
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "SubmissionRepo"

class SubmissionRepo(
        private val redditClient: RedditClient,
        private val prefsDAO: SubredditPrefsDAO,
        private val submissionsCacheDAO: SubmissionsCacheDAO,
        private val subredditName: String,
        private val scope: CoroutineScope
) {
    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastMessage = MutableLiveData(SingleLiveEvent<String?>(null))
    val toastMessage: LiveData<SingleLiveEvent<String?>> = _toastMessage

    val postViewType: LiveData<SubmissionUiType> = prefsDAO.observeViewType(subredditName)
    private val nextPageKeyAndCurrentPageEntries = MutableLiveData<MutableMap<String?, List<String>>>(mutableMapOf())
    val allSubmissions: LiveData<List<LinkData>> =
            Transformations.switchMap(nextPageKeyAndCurrentPageEntries) { pageMap: Map<String?, List<String>> ->
                if (pageMap.isNotEmpty()) {
                    Log.i(TAG, "page keys in map : ${pageMap.keys}")
                    submissionsCacheDAO.observeCachedSubmissions(cachedSubmissionsObserveQueryInOrderOfGivenIds(pageMap.values.flatten()))
                } else MutableLiveData(emptyList())
            }

    private fun cachedSubmissionsObserveQueryInOrderOfGivenIds(ids: List<String>): SimpleSQLiteQuery =
            SimpleSQLiteQuery(
                    StringBuilder().apply {
                        append(" SELECT * FROM Linkdata")
                        append(" WHERE id IN (")
                        append(TextUtils.join(",", ids.map { "\'$it\'" }))
                        append(")")
                        append(" ORDER BY CASE id ")
                        ids.forEachIndexed { index, id -> append(" WHEN \'$id\' THEN $index ") }
                        append(" END")
                    }.toString(), null)

    fun hasNextPage(): Boolean = nextPageKeyAndCurrentPageEntries.value?.keys?.last() != null

    fun togglePostViewType() = scope.launch(Dispatchers.IO) {
        prefsDAO.apply {
            if (postViewType.value == SubmissionUiType.COMPACT)
                setUIType(SubmissionUiType.LARGE, subredditName)
            else setUIType(SubmissionUiType.COMPACT, subredditName)
        }
    }

    fun changeSort(newSort: SubredditSorting) {
        clearSubmissions()
        scope.launch(Dispatchers.IO) { prefsDAO.setSorting(newSort, subredditName) }
        loadPage()
    }

    fun clearSubmissions() {
        nextPageKeyAndCurrentPageEntries.value = mutableMapOf()
    }

    fun loadPage() {
        scope.launch(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                loadPageAndCacheToDbSuccessfully()
            } catch (e: Exception) {
                e.printStackTrace()
                _toastMessage.postValue(SingleLiveEvent("Something went wrong! try again later some later"))
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    @Throws(Exception::class)
    private suspend fun loadPageAndCacheToDbSuccessfully() {
        val redditAPI = redditClient.api()
        val sorting = (prefsDAO.getSubredditSorting(subredditName)
                ?: createAndSaveDefaultSubredditPrefs(subredditName).subredditSorting).mapSorting()
        val fetchedSubmissions = redditAPI.getSubreddit(
                "${if (subredditName != FRONTPAGE) "r/" else ""}$subredditName",
                sorting.first,
                sorting.second,
                nextPageKeyAndCurrentPageEntries.value.run {
                    if (!this.isNullOrEmpty()) keys.last()
                    else null
                }
        )
        fetchedSubmissions.apply {
            submissions.forEach { submissionsCacheDAO.insertSubmissions(it) }
            nextPageKeyAndCurrentPageEntries.postValue(nextPageKeyAndCurrentPageEntries.value?.apply {
                put(after, submissions.map { it.id })
            })
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