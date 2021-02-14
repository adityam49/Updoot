package com.ducktapedapps.updoot.ui.subreddit

import com.ducktapedapps.updoot.data.local.model.LocalSubreddit
import com.ducktapedapps.updoot.data.mappers.toLocalSubreddit
import com.ducktapedapps.updoot.data.remote.model.RemoteSubreddit
import com.ducktapedapps.updoot.ui.subreddit.SubredditInfoState.*
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.accountManagement.IRedditClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

interface GetSubredditInfoUseCase {

    val subredditInfo: StateFlow<SubredditInfoState?>

    suspend fun loadSubredditInfo(subredditName: String)

}

class GetSubredditInfoUseCaseImpl @Inject constructor(
    private val redditClient: IRedditClient,
) : GetSubredditInfoUseCase {

    override val subredditInfo: MutableStateFlow<SubredditInfoState?> = MutableStateFlow(null)

    //TODO : caching and fetching mechanism to repo
    override suspend fun loadSubredditInfo(subredditName: String) {
        if (subredditName == Constants.FRONTPAGE) subredditInfo.value = null
        else {
            subredditInfo.value = Loading

            subredditInfo.value = try {
                val remoteSubreddit = fetchSubredditInfo(subredditName)
                remoteSubreddit.toLocalSubreddit().toUiModel()
            } catch (e: Exception) {
                LoadingFailed(reason = e.message ?: "Something went wrong")
            }
        }
    }


    private suspend fun fetchSubredditInfo(subredditName: String): RemoteSubreddit =
        withContext(Dispatchers.IO) {
            val api = redditClient.api()
            api.getSubredditInfo(subreddit = subredditName)
        }
}

sealed class SubredditInfoState {
    data class UiModel(
        val activeAccounts: Long?,
        val subscribers: Long?,
        val subredditName: String,
        val created: Date,
        val icon: String?,
        val info: String?
    ) : SubredditInfoState()

    data class LoadingFailed(val reason: String) : SubredditInfoState()

    object Loading : SubredditInfoState()
}

private fun LocalSubreddit.toUiModel(): UiModel = UiModel(
    activeAccounts = accountsActive,
    subscribers = subscribers,
    subredditName = subredditName,
    icon = icon,
    info = longDescription,
    created = created
)