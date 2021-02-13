package com.ducktapedapps.updoot.ui.user

import com.ducktapedapps.updoot.data.mappers.toLocalFullComment
import com.ducktapedapps.updoot.ui.user.UserContent.UserComment
import com.ducktapedapps.updoot.utils.Page
import com.ducktapedapps.updoot.utils.Page.*
import com.ducktapedapps.updoot.utils.accountManagement.IRedditClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

interface GetUserCommentsUseCase {

    val pagesOfComments: StateFlow<List<Page<UserComment>>>

    suspend fun loadNextPage(userName: String)

}

//TODO : Use repository for caching
class GetUserCommentsUseCaseImpl @Inject constructor(
    private val redditClient: IRedditClient,
) : GetUserCommentsUseCase {

    override val pagesOfComments: MutableStateFlow<List<Page<UserComment>>> =
        MutableStateFlow(emptyList())

    override suspend fun loadNextPage(userName: String) {
        when (val lastPage = pagesOfComments.value.lastOrNull()) {
            is LoadedPage -> loadAfterNoError(lastPage, userName)
            is ErrorPage -> loadAfterError(lastPage, userName)
            LoadingPage -> Unit
            End -> Unit
            else -> loadAfterNoError(userName = userName)
        }
    }

    private suspend fun loadAfterError(errorPage: ErrorPage, userName: String) {
        pagesOfComments.value = pagesOfComments.value.toMutableList().apply {
            removeLast()
            this += LoadingPage
        }

        pagesOfComments.value = pagesOfComments.value.toMutableList().apply {
            this[size - 1] = try {
                val api = redditClient.api()
                val result = api.getUserComments(userName, errorPage.currentPageKey)
                LoadedPage(
                    content = result.children.map { UserComment(it.toLocalFullComment()) },
                    nextPageKey = result.after
                )
            } catch (e: Exception) {
                errorPage.copy(errorReason = e.message ?: "Something went wrong")
            }
        }
    }

    private suspend fun loadAfterNoError(
        page: LoadedPage<UserComment>? = null,
        userName: String
    ) {
        pagesOfComments.value += LoadingPage

        pagesOfComments.value = pagesOfComments.value.toMutableList().apply {
            val fetchedPage = try {
                val api = redditClient.api()
                val result = api.getUserComments(userName, page?.nextPageKey)
                LoadedPage(
                    result.children.map { UserComment(it.toLocalFullComment()) },
                    result.after
                )
            } catch (e: Exception) {
                ErrorPage(
                    currentPageKey = page?.nextPageKey,
                    errorReason = e.message ?: "Something went wrong"
                )
            }
            this[size - 1] = fetchedPage

            if (fetchedPage is LoadedPage && fetchedPage.nextPageKey == null)
                this += End

        }
    }
}