package com.ducktapedapps.updoot.ui.user

import com.ducktapedapps.updoot.data.mappers.toLocalFullComment
import com.ducktapedapps.updoot.ui.user.UserContent.UserComment
import com.ducktapedapps.updoot.utils.PagingModel
import com.ducktapedapps.updoot.utils.PagingModel.Footer.*
import com.ducktapedapps.updoot.utils.Result
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

interface GetUserCommentsUseCase {

    val pagingModel: StateFlow<PagingModel<List<UserComment>>>

    suspend fun loadNextPage(userName: String)

}

//TODO : Use repository for caching
class GetUserCommentsUseCaseImpl @Inject constructor(
    private val redditClient: RedditClient,
) : GetUserCommentsUseCase {
    override val pagingModel: MutableStateFlow<PagingModel<List<UserComment>>> = MutableStateFlow(
        PagingModel(content = emptyList(), footer = UnLoadedPage(null))
    )

    override suspend fun loadNextPage(userName: String) {
        when (val footer = pagingModel.value.footer) {
            is UnLoadedPage -> {
                pagingModel.value = pagingModel.value.copy(footer = Loading)
                delay(100) // TODO : remove this
                val result = getComments(userName, footer.pageKey)
                when (result) {
                    is Result.Error -> pagingModel.value =
                        pagingModel.value.copy(footer = Error(result.exception, footer.pageKey))
                    is Result.Success -> pagingModel.value = pagingModel.value.copy(
                        content = pagingModel.value.content + result.data.content,
                        footer = result.data.footer
                    )
                }
            }
            is Error -> {
                pagingModel.value = pagingModel.value.copy(footer = Loading)
                val result = getComments(userName, footer.pageKey)
                when (result) {
                    is Result.Error -> pagingModel.value =
                        pagingModel.value.copy(footer = Error(result.exception, footer.pageKey))
                    is Result.Success -> pagingModel.value = pagingModel.value.copy(
                        content = pagingModel.value.content + result.data.content,
                        footer = result.data.footer
                    )
                }
            }
            else -> Unit
        }
    }

    private suspend fun getComments(
        userName: String,
        nextPageKey: String?
    ): Result<PagingModel<List<UserComment>>> =
        try {
            val api = redditClient.api()
            val result = api.getUserComments(userName, nextPageKey)
            Result.Success(
                PagingModel(
                    content = result.children.map { UserComment(it.toLocalFullComment()) },
                    footer = if (result.after != null) UnLoadedPage(pageKey = result.after) else End
                )
            )
        } catch (exception: Exception) {
            Result.Error(exception)
        }
}
