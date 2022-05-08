package com.ducktapedapps.updoot.subreddit

import com.ducktapedapps.updoot.data.local.SubredditDAO
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel.AnonymousAccount
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel.UserModel
import com.ducktapedapps.updoot.utils.accountManagement.UpdootAccountsProvider
import kotlinx.coroutines.flow.*
import javax.inject.Inject

interface GetSubredditSubscriptionState {

    fun getIsSubredditSubscribedState(subredditName: String): Flow<Boolean?>

}

class GetSubredditSubscriptionStateImpl @Inject constructor(
    private val updootAccountsProvider: UpdootAccountsProvider,
    private val subredditDAO: SubredditDAO,
) : GetSubredditSubscriptionState {
    override fun getIsSubredditSubscribedState(subredditName: String): Flow<Boolean?> {

        return updootAccountsProvider
            .getCurrentAccount()
            .flatMapLatest { currentUser ->
                when (currentUser) {
                    is AnonymousAccount ->  flowOf(null)
                    is UserModel -> subredditDAO.observeSubredditSubscription(
                        subredditName = subredditName,
                        currentUser = currentUser.name
                    ).map { it != null }
                }
            }
    }
}