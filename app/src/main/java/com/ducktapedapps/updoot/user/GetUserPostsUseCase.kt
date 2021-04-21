package com.ducktapedapps.updoot.user

import android.text.TextUtils
import androidx.sqlite.db.SimpleSQLiteQuery
import com.ducktapedapps.updoot.data.local.PostDAO
import com.ducktapedapps.updoot.data.local.model.Post
import com.ducktapedapps.updoot.data.mappers.toPost
import com.ducktapedapps.updoot.utils.IdsPage
import com.ducktapedapps.updoot.utils.PagingModel
import com.ducktapedapps.updoot.utils.PagingModel.Footer.*
import com.ducktapedapps.updoot.utils.RedditItem
import com.ducktapedapps.updoot.utils.RedditItem.PostData
import com.ducktapedapps.updoot.utils.Result
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

interface GetUserPostsUseCase {
    val pagingModel: Flow<PagingModel<List<RedditItem>>>

    suspend fun loadNextPage(userName: String)
}

class GetUserPostsUseCaseImpl @Inject constructor(
    private val redditClient: RedditClient,
    private val postDAO: PostDAO,
) : GetUserPostsUseCase {
    private val _pagingModel: MutableStateFlow<PagingModel<List<String>>> = MutableStateFlow(
        PagingModel(
            content = emptyList(),
            footer = UnLoadedPage(null)
        )
    )
    override val pagingModel: Flow<PagingModel<List<RedditItem>>> =
        _pagingModel.transformLatest {
            observeCachedPosts(it.content)
                .distinctUntilChanged()
                .collect { posts ->
                    emit(
                        PagingModel(
                            content = posts.map { post -> PostData(post) }.toList(),
                            footer = it.footer
                        )
                    )
                }
        }


    override suspend fun loadNextPage(userName: String) {
        when (val footer = _pagingModel.value.footer) {
            is UnLoadedPage -> {
                _pagingModel.value = _pagingModel.value.copy(footer = Loading)
                delay(100) // TODO : remove this
                val result = getUserPosts(
                    userName = userName,
                    nextPageKey = footer.pageKey
                )
                when (result) {
                    is Result.Error -> _pagingModel.value =
                        _pagingModel.value.copy(footer = Error(result.exception, footer.pageKey))
                    is Result.Success -> _pagingModel.value = _pagingModel.value.copy(
                        content = _pagingModel.value.content + result.data.ids,
                        footer = if (result.data.hasNextPage()) UnLoadedPage(result.data.nextPageKey) else End
                    )
                }
            }
            is Error -> {
                _pagingModel.value = _pagingModel.value.copy(footer = Loading)
                val result = getUserPosts(
                    userName = userName,
                    nextPageKey = footer.pageKey
                )
                when (result) {
                    is Result.Error -> _pagingModel.value =
                        _pagingModel.value.copy(footer = Error(result.exception, footer.pageKey))
                    is Result.Success -> _pagingModel.value = _pagingModel.value.copy(
                        content = _pagingModel.value.content + result.data.ids,
                        footer = if (result.data.hasNextPage()) UnLoadedPage(result.data.nextPageKey) else End
                    )
                }
            }
            else -> Unit
        }
    }

    private suspend fun getUserPosts(
        userName: String,
        nextPageKey: String?,
    ): Result<IdsPage> =
        try {
            val api = redditClient.api()
            val results = api.getUserSubmittedPosts(userName, nextPageKey)
            val fetchedPosts = results.children.map { it.toPost() }
            fetchedPosts.cache()
            Result.Success(IdsPage(fetchedPosts.map { it.id }, results.after))
        } catch (exception: Exception) {
            Result.Error(exception)
        }

    private fun observeCachedPosts(ids: List<String>): Flow<List<Post>> =
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
}