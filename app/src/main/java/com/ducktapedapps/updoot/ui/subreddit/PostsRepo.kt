package com.ducktapedapps.updoot.ui.subreddit

import android.text.TextUtils
import androidx.sqlite.db.SimpleSQLiteQuery
import com.ducktapedapps.updoot.data.local.PostDAO
import com.ducktapedapps.updoot.data.local.model.Post
import com.ducktapedapps.updoot.data.mappers.toPost
import com.ducktapedapps.updoot.ui.subreddit.PageResource.Error
import com.ducktapedapps.updoot.ui.subreddit.PageResource.Success
import com.ducktapedapps.updoot.ui.subreddit.SubredditSorting.*
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.Constants.FRONTPAGE
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PostsRepo @Inject constructor(
    private val redditClient: RedditClient,
    private val postDAO: PostDAO,
) {

    //TODO : make caching mechanism
    suspend fun getPage(
        subreddit: String,
        nextPageKey: String?,
        sorting: SubredditSorting,
    ): PageResource = withContext(Dispatchers.IO) {
        try {
            val redditAPI = redditClient.api()
            val s = sorting.mapSorting()
            val listing = redditAPI.getSubmissions(
                subreddit = "${if (subreddit != FRONTPAGE) "r/" else ""}$subreddit",
                sort = s.first,
                time = s.second,
                after = nextPageKey
            )

            val fetchedPosts = listing.children.map { it.toPost() }

            fetchedPosts.cache()

            Success(
                content = postDAO.observeCachedPosts(buildSqlQuery(fetchedPosts.map { it.id })),
                nextPageKey = listing.after
            )
        } catch (e: Exception) {
            Error(e.message ?: "Something went wrong")
        }
    }

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
            }.toString(), null
        )

    private suspend fun List<Post>.cache() {
        forEach { postDAO.insertPost(it) }
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

sealed class PageResource {

    data class Success(val content: Flow<List<Post>>, val nextPageKey: String?) : PageResource()

    data class Error(val reason: String) : PageResource()

}