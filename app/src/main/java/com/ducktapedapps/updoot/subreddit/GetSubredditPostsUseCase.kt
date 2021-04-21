package com.ducktapedapps.updoot.subreddit

import android.text.TextUtils
import androidx.sqlite.db.SimpleSQLiteQuery
import com.ducktapedapps.updoot.data.local.PostDAO
import com.ducktapedapps.updoot.data.local.model.Post
import com.ducktapedapps.updoot.data.mappers.toPost
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.IdsPage
import com.ducktapedapps.updoot.utils.PagingModel
import com.ducktapedapps.updoot.utils.PagingModel.Footer.*
import com.ducktapedapps.updoot.utils.Result
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface GetSubredditPostsUseCase {

    val pagingModel: Flow<PagingModel<List<Post>>>

    suspend fun loadNextPage(
        subreddit: String,
        reload: Boolean = false,
        subredditSorting: SubredditSorting
    )
}

class GetSubredditPostsUseCaseImpl @Inject constructor(
    private val redditClient: RedditClient,
    private val postDAO: PostDAO,
) : GetSubredditPostsUseCase {
    private val _pagingModel: MutableStateFlow<PagingModel<List<String>>> = MutableStateFlow(
        PagingModel(
            content = emptyList(),
            footer = UnLoadedPage(null)
        )
    )

    override val pagingModel = _pagingModel.transformLatest {
        observeCachedPosts(it.content)
            .distinctUntilChanged()
            .collect { cachedPosts ->
                emit(
                    PagingModel(
                        content = cachedPosts,
                        footer = it.footer
                    )
                )
            }
    }

    override suspend fun loadNextPage(
        subreddit: String,
        reload: Boolean,
        subredditSorting: SubredditSorting
    ) {
        when (val data = _pagingModel.value.footer) {
            is UnLoadedPage -> {
                _pagingModel.value = _pagingModel.value.copy(footer = Loading)
                delay(100) // TODO : remove this
                val result = getSubredditPage(
                    subreddit = subreddit,
                    sorting = subredditSorting,
                    nextPageKey = data.pageKey
                )
                when (result) {
                    is Result.Error -> _pagingModel.value =
                        _pagingModel.value.copy(footer = Error(result.exception, data.pageKey))
                    is Result.Success -> _pagingModel.value = _pagingModel.value.copy(
                        content = _pagingModel.value.content + result.data.ids,
                        footer = if (result.data.hasNextPage()) UnLoadedPage(result.data.nextPageKey) else End
                    )
                }
            }
            is Error -> {
                _pagingModel.value = _pagingModel.value.copy(footer = Loading)
                val result = getSubredditPage(
                    subreddit = subreddit,
                    sorting = subredditSorting,
                    nextPageKey = data.pageKey
                )
                when (result) {
                    is Result.Error -> _pagingModel.value =
                        _pagingModel.value.copy(footer = Error(result.exception, data.pageKey))
                    is Result.Success -> _pagingModel.value = _pagingModel.value.copy(
                        content = _pagingModel.value.content + result.data.ids,
                        footer = if (result.data.hasNextPage()) UnLoadedPage(result.data.nextPageKey) else End
                    )
                }
            }
            else -> Unit
        }
    }

    suspend fun getSubredditPage(
        subreddit: String,
        nextPageKey: String?,
        sorting: SubredditSorting,
    ): Result<IdsPage> = withContext(Dispatchers.IO) {
        try {
            val redditAPI = redditClient.api()
            val s = sorting.mapSorting()
            val listing = redditAPI.getSubmissions(
                subreddit = "${if (subreddit != Constants.FRONTPAGE) "r/" else ""}$subreddit",
                sort = s.first,
                time = s.second,
                after = nextPageKey
            )

            val fetchedPosts = listing.children.map { it.toPost() }

            fetchedPosts.cache()

            Result.Success(
                IdsPage(
                    ids = fetchedPosts.map { it.id },
                    nextPageKey = listing.after
                )
            )
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun observeCachedPosts(ids: List<String>): Flow<List<Post>> =
        if (ids.isNotEmpty())
            postDAO.observeCachedPosts(buildSqlQuery(ids))
        else
            flow { emit(emptyList<Post>()) }

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
        SubredditSorting.Rising -> Pair(Constants.RISING, null)
        SubredditSorting.Best -> Pair(Constants.BEST, null)
        SubredditSorting.New -> Pair(Constants.NEW, null)
        SubredditSorting.Hot -> Pair(Constants.HOT, null)

        SubredditSorting.TopHour -> Pair(Constants.TOP, Constants.NOW)
        SubredditSorting.TopDay -> Pair(Constants.TOP, Constants.TODAY)
        SubredditSorting.TopWeek -> Pair(Constants.TOP, Constants.THIS_WEEK)
        SubredditSorting.TopMonth -> Pair(Constants.TOP, Constants.THIS_MONTH)
        SubredditSorting.TopYear -> Pair(Constants.TOP, Constants.THIS_YEAR)
        SubredditSorting.TopAll -> Pair(Constants.TOP, Constants.ALL_TIME)

        SubredditSorting.ControversialHour -> Pair(Constants.CONTROVERSIAL, Constants.NOW)
        SubredditSorting.ControversialDay -> Pair(Constants.CONTROVERSIAL, Constants.TODAY)
        SubredditSorting.ControversialWeek -> Pair(Constants.CONTROVERSIAL, Constants.THIS_WEEK)
        SubredditSorting.ControversialMonth -> Pair(Constants.CONTROVERSIAL, Constants.THIS_MONTH)
        SubredditSorting.ControversialYear -> Pair(Constants.CONTROVERSIAL, Constants.THIS_YEAR)
        SubredditSorting.ControversialAll -> Pair(Constants.CONTROVERSIAL, Constants.ALL_TIME)
    }
}