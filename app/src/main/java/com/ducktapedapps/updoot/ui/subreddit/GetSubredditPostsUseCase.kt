package com.ducktapedapps.updoot.ui.subreddit

import com.ducktapedapps.updoot.utils.Page
import com.ducktapedapps.updoot.utils.Page.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface GetSubredditPostsUseCase {

    val pagesOfPosts: StateFlow<List<Page<PostUiModel>>>

    suspend fun loadNextPage(
        subreddit: String,
        reload: Boolean = false,
        subredditSorting: SubredditSorting
    )
}

class GetSubredditPostsUseCaseImpl @Inject constructor(
    private val postsRepo: PostsRepo
) : GetSubredditPostsUseCase {
    override val pagesOfPosts: MutableStateFlow<List<Page<PostUiModel>>> = MutableStateFlow(
        emptyList()
    )

    override suspend fun loadNextPage(
        subreddit: String,
        reload: Boolean,
        subredditSorting: SubredditSorting
    ) {

        if (reload) pagesOfPosts.value = emptyList()

        when (val lastPage = pagesOfPosts.value.lastOrNull()) {
            null -> loadAfterNoError(subredditName = subreddit, subredditSorting = subredditSorting)
            is ErrorPage -> loadAfterError(
                subredditName = subreddit,
                errorPage = lastPage,
                sorting = subredditSorting
            )
            is LoadedPage -> loadAfterNoError(
                subredditName = subreddit,
                page = lastPage,
                subredditSorting = subredditSorting
            )
            LoadingPage -> Unit
            End -> Unit
        }
    }

    private suspend fun loadAfterError(
        subredditName: String,
        sorting: SubredditSorting,
        errorPage: ErrorPage
    ) {
        pagesOfPosts.value = pagesOfPosts.value.toMutableList().apply {
            removeLast()
            this += LoadingPage
        }
        //TODO : Remove
        delay(1000)

        pagesOfPosts.value = pagesOfPosts.value.toMutableList().apply {
            val fetchedPage = getPageFromRepo(
                subreddit = subredditName,
                sorting = sorting,
                pageKey = errorPage.currentPageKey
            )
            this[size - 1] = fetchedPage
        }
    }

    private suspend fun loadAfterNoError(
        page: LoadedPage<PostUiModel>? = null,
        subredditName: String,
        subredditSorting: SubredditSorting,
    ) {
        pagesOfPosts.value += LoadingPage

        pagesOfPosts.value = pagesOfPosts.value.toMutableList().apply {

            val fetchedPage = getPageFromRepo(subredditName, page?.nextPageKey, subredditSorting)

            delay(500)
            this[size - 1] = fetchedPage

            if (fetchedPage is LoadedPage && fetchedPage.nextPageKey == null)
                this += End

        }
    }

    private suspend fun getPageFromRepo(
        subreddit: String,
        pageKey: String?,
        sorting: SubredditSorting,
    ) = when (
        val response = postsRepo.getPage(
            subreddit = subreddit,
            nextPageKey = pageKey,
            sorting = sorting
        )
    ) {
        is PageResource.Error -> ErrorPage(
            currentPageKey = pageKey,
            errorReason = response.reason
        )
        is PageResource.Success -> LoadedPage(
            content = response.content.map { it.map { post -> post.toUiModel() } },
            nextPageKey = response.nextPageKey,
        )
    }
}