package com.ducktapedapps.updoot.ui.subreddit

import android.text.TextUtils
import androidx.sqlite.db.SimpleSQLiteQuery
import com.ducktapedapps.updoot.data.local.PostDAO
import com.ducktapedapps.updoot.data.local.SubredditDAO
import com.ducktapedapps.updoot.data.local.SubredditPrefs
import com.ducktapedapps.updoot.data.local.SubredditPrefsDAO
import com.ducktapedapps.updoot.data.local.model.LocalSubreddit
import com.ducktapedapps.updoot.data.local.model.Post
import com.ducktapedapps.updoot.data.mappers.toLocalSubreddit
import com.ducktapedapps.updoot.data.mappers.toPost
import com.ducktapedapps.updoot.data.remote.model.LinkData
import com.ducktapedapps.updoot.data.remote.model.Listing
import com.ducktapedapps.updoot.data.remote.model.RemoteSubreddit
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
        private val postCacheDAO: PostDAO,
        private val subredditDAO: SubredditDAO,
) {
    fun subredditPrefs(subredditName: String): Flow<SubredditPrefs?> = prefsDAO
            .observeSubredditPrefs(subredditName)
            .distinctUntilChanged()

    fun subredditInfo(subredditName: String): Flow<LocalSubreddit?> =
            if (subredditName == FRONTPAGE) flow { emit(null) }
            else subredditDAO.observeSubredditInfo(subredditName)
                    .transform {
                        if (it == null) {
                            val fetchedSubreddit = fetchSubredditInfo(subredditName)
                            subredditDAO.insertSubreddit(fetchedSubreddit.toLocalSubreddit())
                        } else emit(it)
                    }.distinctUntilChanged()


    private suspend fun fetchSubredditInfo(subreddit: String): RemoteSubreddit = withContext(Dispatchers.IO) {
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

    suspend fun vote(id: String, direction: Int) {}


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

    fun observeCachedSubmissions(ids: List<String>): Flow<List<Post>> =
            postCacheDAO.observeCachedPosts(buildSqlQuery(ids))

    private fun buildSqlQuery(ids: List<String>): SimpleSQLiteQuery =
            SimpleSQLiteQuery(
                    StringBuilder().apply {
                        append("SELECT * FROM Post")
                        append(" WHERE id IN (")
                        append(TextUtils.join(",", ids.map { "\'$it\'" }))
                        append(")")
                        append(" ORDER BY CASE id ")
                        ids.forEachIndexed { index, id -> append(" WHEN \'$id\' THEN $index ") }
                        append(" END")
                    }.toString(), null)

    suspend fun cacheSubmissions(submissions: List<LinkData>) = submissions.map {
        it.toPost()
    }.forEach { post ->
        withContext(Dispatchers.IO) {
            postCacheDAO.insertPost(post)
        }
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