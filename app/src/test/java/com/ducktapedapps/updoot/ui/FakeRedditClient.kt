package com.ducktapedapps.updoot.ui

import android.content.Context
import com.ducktapedapps.updoot.data.local.model.Token
import com.ducktapedapps.updoot.data.remote.RedditAPI
import com.ducktapedapps.updoot.ui.navDrawer.AccountModel
import com.ducktapedapps.updoot.ui.navDrawer.AllNavigationEntries
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.accountManagement.IRedditClient

class FakeRedditClient : IRedditClient {
    val loggedInUserName = "user1"
    private var currentAccount = Constants.ANON_USER

    val nonLoggedInDestinations = AllNavigationEntries.filter { it.isUserSpecific }
    val loggedInDestinations = AllNavigationEntries

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