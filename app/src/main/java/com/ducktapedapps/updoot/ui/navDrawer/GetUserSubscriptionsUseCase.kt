package com.ducktapedapps.updoot.ui.navDrawer

import com.ducktapedapps.updoot.data.local.SubredditDAO
import com.ducktapedapps.updoot.data.local.model.LocalSubreddit
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel.AnonymousAccount
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel.UserModel
import com.ducktapedapps.updoot.utils.accountManagement.IRedditClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface GetUserSubscriptionsUseCase {

    val subscriptions: Flow<List<LocalSubreddit>>

}

class GetUserSubscriptionsUseCaseImpl @Inject constructor(
    private val subredditDAO: SubredditDAO,
    redditClient: IRedditClient,
) : GetUserSubscriptionsUseCase {

    override val subscriptions: Flow<List<LocalSubreddit>> = redditClient.allAccounts
        .map { accounts -> accounts.first { account -> account.isCurrent } }
        .flatMapLatest {
            when (it) {
                is AnonymousAccount -> flowOf(emptyList())
                is UserModel -> {
                    delay(500)
                    subredditDAO.observeSubscribedSubredditsFor(it.name)
                }
            }
        }
}
