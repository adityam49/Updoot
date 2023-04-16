package com.ducktapedapps.updoot.subreddit

import com.ducktapedapps.updoot.data.local.SubredditDAO
import com.ducktapedapps.updoot.data.local.SubredditSubscription
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import com.ducktapedapps.updoot.utils.accountManagement.UpdootAccountsProvider
import kotlinx.coroutines.flow.first
import javax.inject.Inject

interface EditSubredditSubscriptionUseCase {

    suspend fun toggleSubscription(subredditName: String)

}

class EditSubredditSubscriptionUseCaseImpl @Inject constructor(
    private val updootAccountsProvider: UpdootAccountsProvider,
    private val subredditDAO: SubredditDAO,
    private val redditClient: RedditClient,
) : EditSubredditSubscriptionUseCase {

    override suspend fun toggleSubscription(subredditName: String) {
        val loggedInUser = updootAccountsProvider
            .isLoggedInUser().first()
        if (loggedInUser != null) {
            val subscription = subredditDAO.observeSubredditSubscription(
                subredditName = subredditName,
                loggedInUser.name
            ).first()

            editSubscriptionRemotely(subredditName, loggedInUser.name, subscription != null)
        }
    }

    private suspend fun editSubscriptionRemotely(
        subredditName: String,
        currentUserName: String,
        isAlreadySubscribed: Boolean
    ) {
        try {
            val api = redditClient.api()
            if (isAlreadySubscribed) {
                val result = api.subscribe(action = "unsub", subredditName = subredditName)
                if (result.isSuccessful)
                    subredditDAO.deleteSubscription(subredditName,currentUserName)
            } else {
                val result = api.subscribe(action = "sub", subredditName = subredditName)
                if (result.isSuccessful) subredditDAO.insertSubscription(
                    SubredditSubscription(
                        subredditName = subredditName,
                        userName = currentUserName
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}