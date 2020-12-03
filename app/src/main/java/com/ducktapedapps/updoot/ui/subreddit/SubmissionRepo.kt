package com.ducktapedapps.updoot.ui.subreddit

import android.text.TextUtils
import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import com.ducktapedapps.updoot.data.local.SubmissionsCacheDAO
import com.ducktapedapps.updoot.data.local.SubredditDAO
import com.ducktapedapps.updoot.data.local.SubredditPrefs
import com.ducktapedapps.updoot.data.local.SubredditPrefsDAO
import com.ducktapedapps.updoot.data.local.model.LinkData
import com.ducktapedapps.updoot.data.local.model.Listing
import com.ducktapedapps.updoot.data.local.model.Subreddit
import com.ducktapedapps.updoot.ui.subreddit.SubredditSorting.*
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.Constants.FRONTPAGE
import com.ducktapedapps.updoot.utils.SubmissionUiType
import com.ducktapedapps.updoot.utils.accountManagement.IRedditClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "SubmissionRepo"

class SubmissionRepo @Inject constructor(
        private val redditClient: IRedditClient,
        private val prefsDAO: SubredditPrefsDAO,
        private val submissionsCacheDAO: SubmissionsCacheDAO,
        private val subredditDAO: SubredditDAO
) {
    fun subredditPrefs(subredditName: String): Flow<SubredditPrefs?> = prefsDAO
            .observeSubredditPrefs(subredditName)
            .distinctUntilChanged()

    fun subredditInfo(subredditName: String): Flow<Subreddit?> =
            if (subredditName == FRONTPAGE) flow { emit(null) }
            else subredditDAO.observeSubredditInfo(subredditName)
                    .transform {
                        if (it == null) {
                            val subreddit = fetchSubredditInfo(subredditName)
                            subredditDAO.insertSubreddit(subreddit.copy(lastUpdated = System.currentTimeMillis()))
                        } else emit(it)
                    }.distinctUntilChanged()


    private suspend fun fetchSubredditInfo(subreddit: String): Subreddit = withContext(Dispatchers.IO) {
        val api = redditClient.api()
        api.getSubredditInfo(subreddit)
    }


    suspend fun setPostViewType(subreddit: String, type: SubmissionUiType) {
        withContext(Dispatchers.IO) { prefsDAO.setUIType(type, subreddit) }
    }

    suspend fun changeSort(subreddit: String, newSort: SubredditSorting) {
        withContext(Dispatchers.IO) {
            prefsDAO.setSorting(newSort, subreddit)
        }
    }

    suspend fun vote(id: String, direction: Int) {
        withContext(Dispatchers.IO) {
            val cachedSubmission = submissionsCacheDAO.getLinkData(id)
            if (cachedSubmission.archived)
                throw IllegalArgumentException("Can't vote on archived submission with id : $id")
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
                    429 -> throw Exception("Too many vote requests!")
                    else -> Log.e(TAG, result.message())
                }
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
            val api = redditClient.api()
            val cachedSubmission = submissionsCacheDAO.getLinkData(id)
            val result = api.run { if (cachedSubmission.saved) unSave(cachedSubmission.name) else save(cachedSubmission.name) }
            when (result.code()) {
                200 -> submissionsCacheDAO.insertSubmissions(cachedSubmission.copy(saved = !cachedSubmission.saved))
                else -> Log.e(TAG, result.message())
            }
        }
    }

    suspend fun getPage(
            subreddit: String,
            nextPageKey: String?,
            sorting: SubredditSorting,
    ): Listing<LinkData> = withContext(Dispatchers.IO) {
        val redditAPI = redditClient.api()
        val s = sorting.mapSorting()
        redditAPI.getSubmissions(
                subreddit = "${if (subreddit != FRONTPAGE) "r/" else ""}$subreddit",
                sort = s.first,
                time = s.second,
                after = nextPageKey
        )
    }

    fun observeCachedSubmissions(ids: List<String>): Flow<List<LinkData>> =
            submissionsCacheDAO.observeCachedSubmissions(buildSqlQuery(ids))

    private fun buildSqlQuery(ids: List<String>): SimpleSQLiteQuery =
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

    suspend fun cacheSubmissions(submissions: List<LinkData>) = submissions.forEach {
        withContext(Dispatchers.IO) { submissionsCacheDAO.insertSubmissions(it) }
    }

    suspend fun saveDefaultSubredditPrefs(subredditName: String) {
        prefsDAO.insertSubredditPrefs(SubredditPrefs(subredditName, SubmissionUiType.COMPACT, Hot))
    }

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