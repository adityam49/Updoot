package com.ducktapedapps.updoot.ui

import android.content.Context
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.api.remote.RedditAPI
import com.ducktapedapps.updoot.model.Token
import com.ducktapedapps.updoot.ui.navDrawer.accounts.AccountModel
import com.ducktapedapps.updoot.ui.navDrawer.destinations.NavDrawerItemModel
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.accountManagement.IRedditClient

class FakeRedditClient : IRedditClient {
    val loggedInUserName = "user1"
    private var currentAccount = Constants.ANON_USER

    val nonLoggedInDestinations: List<NavDrawerItemModel> = listOf(
            NavDrawerItemModel("Explore", R.drawable.ic_explore_24dp),
            NavDrawerItemModel("History", R.drawable.ic_baseline_history_24)
    ).toList()
    val loggedInDestinations: List<NavDrawerItemModel> = listOf(
            NavDrawerItemModel("Create Post", R.drawable.ic_baseline_edit_24),
            NavDrawerItemModel("Inbox", R.drawable.ic_baseline_inbox_24),
            NavDrawerItemModel("Profile", R.drawable.ic_account_circle_24dp)
    ).toList()

    override fun attachListener(context: Context) {
        TODO("Not yet implemented")
    }

    override fun detachListener() {
        TODO("Not yet implemented")
    }

    override suspend fun api(): RedditAPI {
        TODO("Not yet implemented")
    }

    override fun createUserAccountAndSetItAsCurrent(username: String, icon: String, token: Token) {
        TODO("Not yet implemented")
    }

    override fun setCurrentAccount(name: String) {
        currentAccount = name
    }

    override fun getAccountModels(): List<AccountModel> = mutableListOf<AccountModel>().apply {
        if (currentAccount == Constants.ANON_USER) {
            add(AccountModel.AnonymousAccount(true))
            add(AccountModel.UserModel(loggedInUserName, false, ""))
        } else {
            add(AccountModel.UserModel(loggedInUserName, true, ""))
            add(AccountModel.AnonymousAccount(false))
        }
        add(AccountModel.AddAccount)
    }

    override suspend fun removeUser(accountName: String): Boolean {
        TODO("Not yet implemented")
    }
}