package com.ducktapedapps.updoot.ui.subreddit

import com.ducktapedapps.updoot.utils.Page
import com.ducktapedapps.updoot.utils.Page.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

interface GetSubredditPostsUseCase {

    val pagesOfPosts: Flow<List<Page<List<PostUiModel>>>>

    suspend fun loadNextPage(
        subreddit: String,
        reload: Boolean = false,
        subredditSorting: SubredditSorting
    )
}

class GetSubredditPostsUseCaseImpl @Inject constructor(
    private val postsRepo: PostsRepo
) : GetSubredditPostsUseCase {
    private val _pagesOfPosts: MutableStateFlow<List<Page<Flow<List<PostUiModel>>>>> =
        MutableStateFlow(
            emptyList()
        )

    override val pagesOfPosts: Flow<List<Page<List<PostUiModel>>>> = _pagesOfPosts.map { allPages ->
        allPages.map { page ->
            when (page) {
                End -> End
                is ErrorPage -> page
                is LoadedPage -> LoadedPage(
                    content = page.content.distinctUntilChanged().first(),
                    nextPageKey = page.nextPageKey
                )
                LoadingPage -> LoadingPage
            }
        }
    }

    override suspend fun loadNextPage(
        subreddit: String,
        reload: Boolean,
        subredditSorting: SubredditSorting
    ) {

        if (reload) _pagesOfPosts.value = emptyList()

        when (val lastPage = _pagesOfPosts.value.lastOrNull()) {
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
        _pagesOfPosts.value = _pagesOfPosts.value.toMutableList().apply {
            removeLast()
            this += LoadingPage
        }

        _pagesOfPosts.value = _pagesOfPosts.value.toMutableList().apply {
            val fetchedPage = getPageFromRepo(
                subreddit = subredditName,
                sorting = sorting,
                pageKey = errorPage.currentPageKey
            )
            this[size - 1] = fetchedPage
        }
    }

    private suspend fun loadAfterNoError(
        page: LoadedPage<Flow<List<PostUiModel>>>? = null,
        subredditName: String,
        subredditSorting: SubredditSorting,
    ) {
        _pagesOfPosts.value += LoadingPage

        _pagesOfPosts.value = _pagesOfPosts.value.toMutableList().apply {

            val fetchedPage = getPageFromRepo(subredditName, page?.nextPageKey, subredditSorting)

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