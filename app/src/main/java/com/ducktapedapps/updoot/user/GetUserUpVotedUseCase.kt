package com.ducktapedapps.updoot.user

import com.ducktapedapps.updoot.data.mappers.toLocalFullComment
import com.ducktapedapps.updoot.data.mappers.toPost
import com.ducktapedapps.updoot.data.remote.model.Comment
import com.ducktapedapps.updoot.data.remote.model.LinkData
import com.ducktapedapps.updoot.utils.PagingModel
import com.ducktapedapps.updoot.utils.RedditItem
import com.ducktapedapps.updoot.utils.Result
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

interface GetUserUpVotedUseCase {
    val pagingModel: StateFlow<PagingModel<List<RedditItem>>>

    suspend fun loadNextPage(userName: String)
}

class GetUserUpVotedUseCaseImpl @Inject constructor(
    private val redditClient: RedditClient
) : GetUserUpVotedUseCase {

    override val pagingModel: MutableStateFlow<PagingModel<List<RedditItem>>> =
        MutableStateFlow(
            PagingModel(
                content = emptyList(),
                footer = PagingModel.Footer.UnLoadedPage(null)
            )
        )

    override suspend fun loadNextPage(userName: String) {
        when (val footer = pagingModel.value.footer) {
            is PagingModel.Footer.UnLoadedPage -> {
                pagingModel.value = pagingModel.value.copy(footer = PagingModel.Footer.Loading)
                delay(100) // TODO : remove this
                val result = getUpVotedItems(userName, footer.pageKey)
                when (result) {
                    is Result.Error -> pagingModel.value =
                        pagingModel.value.copy(
                            footer = PagingModel.Footer.Error(
                                result.exception,
                                footer.pageKey
                            )
                        )
                    is Result.Success -> pagingModel.value = pagingModel.value.copy(
                        content = pagingModel.value.content + result.data.content,
                        footer = result.data.footer
                    )
                }
            }
            is PagingModel.Footer.Error -> {
                pagingModel.value =
                    pagingModel.value.copy(footer = PagingModel.Footer.Loading)
                val result = getUpVotedItems(userName, footer.pageKey)
                when (result) {
                    is Result.Error -> pagingModel.value =
                        pagingModel.value.copy(
                            footer = PagingModel.Footer.Error(
                                result.exception,
                                footer.pageKey
                            )
                        )
                    is Result.Success -> pagingModel.value = pagingModel.value.copy(
                        content = pagingModel.value.content + result.data.content,
                        footer = result.data.footer
                    )
                }
            }
            else -> Unit
        }
    }

    private suspend fun getUpVotedItems(
        userName: String,
        nextPageKey: String?
    ): Result<PagingModel<List<RedditItem>>> =
        try {
            val api = redditClient.api()
            val result = api.getUserUpVotedThings(userName, nextPageKey)
            val items = mutableListOf<RedditItem>()
            result.children.forEach {
                if (it is Comment.CommentData) items.add(
                    RedditItem.CommentData(
                        it.toLocalFullComment()
                    )
                )
                else if (it is LinkData) items.add(
                    RedditItem.PostData(
                        it.toPost()
                    )
                )
            }
            Result.Success(
                PagingModel(
                    content = items,
                    footer = if (result.after != null) PagingModel.Footer.UnLoadedPage(
                        pageKey = result.after
                    )
                    else PagingModel.Footer.End
                )
            )
        } catch (exception: Exception) {
            Result.Error(exception)
        }
}
