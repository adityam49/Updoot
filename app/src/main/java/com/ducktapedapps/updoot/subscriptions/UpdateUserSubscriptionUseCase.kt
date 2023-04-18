package com.ducktapedapps.updoot.subscriptions

import com.ducktapedapps.updoot.data.local.SubredditDAO
import com.ducktapedapps.updoot.data.local.SubredditSubscription
import com.ducktapedapps.updoot.data.local.model.LocalSubreddit
import com.ducktapedapps.updoot.data.mappers.toLocalSubreddit
import com.ducktapedapps.updoot.data.remote.model.RemoteSubreddit
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import com.ducktapedapps.updoot.utils.accountManagement.UpdootAccountManager
import com.ducktapedapps.updoot.utils.accountManagement.UpdootAccountsProvider
import kotlinx.coroutines.flow.first
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface UpdateUserSubscriptionUseCase {

    suspend fun updateUserSubscription(userName: String): Boolean

}

class UpdateUserSubscriptionUseCaseImpl @Inject constructor(
    private val redditClient: RedditClient,
    private val subredditDAO: SubredditDAO,
    private val accountsProvider: UpdootAccountsProvider,
    private val accountManager: UpdootAccountManager,
) : UpdateUserSubscriptionUseCase {

    override suspend fun updateUserSubscription(userName: String): Boolean {
        return try {
            val account = accountsProvider
                .getLoggedInUsers()
                .first()
                .first { it.name == userName }
            if (TimeUnit.MILLISECONDS.toMinutes(Date().time - account.lastSyncedAccountData.time ) < 5) {
                return false
            }
            val results = getUpdatedUserSubscription()
            val cachedSubs = results.cacheSubreddits()
            editLocalSubscriptions(userName, cachedSubs)
            accountManager.setLastSyncedAt(timeStamp = Date(), username = account.name)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    private suspend fun getUpdatedUserSubscription(): List<RemoteSubreddit> {
        val redditAPI = redditClient.api()
        var result = redditAPI.getSubscribedSubreddits(null)
        val allSubs = mutableListOf<RemoteSubreddit>().apply {
            addAll(result.children)
        }
        var after: String? = result.after
        while (after != null) {
            result = redditAPI.getSubscribedSubreddits(after)
            allSubs.addAll(result.children)
            after = result.after
        }
        return allSubs
    }

    private suspend fun List<RemoteSubreddit>.cacheSubreddits(): List<LocalSubreddit> {
        return map { subreddits ->
            subreddits.toLocalSubreddit().copy(lastUpdated = Date())
        }.apply {
            subredditDAO.insertSubreddits(this)
        }
    }

    private suspend fun editLocalSubscriptions(userName: String, subs: List<LocalSubreddit>) {
        subredditDAO.deleteUserSubscriptions(userName)
        subs.map {
            SubredditSubscription(
                userName = userName,
                subredditName = it.subredditName
            )
        }.apply {
            subredditDAO.insertSubscriptions(this)
        }
    }
}