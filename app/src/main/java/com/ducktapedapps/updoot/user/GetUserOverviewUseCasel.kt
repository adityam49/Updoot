package com.ducktapedapps.updoot.user

import com.ducktapedapps.updoot.data.mappers.toLocalFullComment
import com.ducktapedapps.updoot.data.mappers.toPost
import com.ducktapedapps.updoot.data.remote.model.Comment
import com.ducktapedapps.updoot.data.remote.model.LinkData
import com.ducktapedapps.updoot.utils.PagingModel
import com.ducktapedapps.updoot.utils.PagingModel.Footer.*
import com.ducktapedapps.updoot.utils.RedditItem
import com.ducktapedapps.updoot.utils.RedditItem.CommentData
import com.ducktapedapps.updoot.utils.RedditItem.PostData
import com.ducktapedapps.updoot.utils.Result
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

interface GetUserOverviewUseCase {
    val pagingModel: Flow<PagingModel<List<RedditItem>>>

    suspend fun loadNextPage(userName: String)

}

class GetUserOverviewUseCaseImpl @Inject constructor(
    private val redditClient: RedditClient,
) : GetUserOverviewUseCase {

    override val pagingModel: MutableStateFlow<PagingModel<List<RedditItem>>> = MutableStateFlow(
        PagingModel(content = emptyList(), footer = UnLoadedPage(null))
    )

    override suspend fun loadNextPage(userName: String) {
        when (val footer = pagingModel.value.footer) {
            is UnLoadedPage -> {
                pagingModel.value = pagingModel.value.copy(footer = Loading)
                delay(100) // TODO : remove this
                val result = getOverViewItems(userName, footer.pageKey)
                when (result) {
                    is Result.Error -> pagingModel.value =
                        pagingModel.value.copy(
                            footer = Error(
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
            is Error -> {
                pagingModel.value = pagingModel.value.copy(footer = Loading)
                val result = getOverViewItems(userName, footer.pageKey)
                when (result) {
                    is Result.Error -> pagingModel.value =
                        pagingModel.value.copy(
                            footer = Error(
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

    private suspend fun getOverViewItems(
        userName: String,
        nextPageKey: String?
    ): Result<PagingModel<List<RedditItem>>> =
        try {
            val api = redditClient.api()
            val result = api.getUserOverView(userName, nextPageKey)
            val items = mutableListOf<RedditItem>()
            result.children.forEach {
                if (it is Comment.CommentData) items.add(CommentData(it.toLocalFullComment()))
                else if (it is LinkData) items.add(PostData(it.toPost()))
            }
            Result.Success(
                PagingModel(
                    content = items,
                    footer = if (result.after != null) UnLoadedPage(pageKey = result.after)
                    else End
                )
            )
        } catch (exception: Exception) {
            Result.Error(exception)
        }

}