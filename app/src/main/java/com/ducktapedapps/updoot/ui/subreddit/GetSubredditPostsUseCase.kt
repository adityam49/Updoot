package com.ducktapedapps.updoot.ui.subreddit

import com.ducktapedapps.updoot.data.local.model.Post
import com.ducktapedapps.updoot.utils.PagingModel
import com.ducktapedapps.updoot.utils.PagingModel.Footer.*
import com.ducktapedapps.updoot.utils.Result
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

interface GetSubredditPostsUseCase {

    val pagingModel: Flow<PagingModel<Flow<List<Post>>>>

    suspend fun loadNextPage(
        subreddit: String,
        reload: Boolean = false,
        subredditSorting: SubredditSorting
    )
}

class GetSubredditPostsUseCaseImpl @Inject constructor(
    private val postsRepo: PostsRepo
) : GetSubredditPostsUseCase {
    private val _pagingModel: MutableStateFlow<PagingModel<List<String>>> = MutableStateFlow(
        PagingModel(
            content = emptyList(),
            footer = UnLoadedPage(null)
        )
    )

    override val pagingModel = _pagingModel.mapLatest {
        PagingModel(
            content = if (it.content.isNotEmpty()) postsRepo
                .observeCachedPosts(it.content)
                .distinctUntilChanged()
            else emptyFlow(),
            footer = it.footer
        )
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
                val result = postsRepo.getPage(
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
                val result = postsRepo.getPage(
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
}