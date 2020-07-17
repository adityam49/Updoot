package com.ducktapedapps.updoot.ui

import androidx.annotation.FloatRange
import androidx.lifecycle.*
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.api.local.SubredditDAO
import com.ducktapedapps.updoot.model.Subreddit
import com.ducktapedapps.updoot.ui.LoginState.LoggedIn
import com.ducktapedapps.updoot.ui.LoginState.LoggedOut
import com.ducktapedapps.updoot.ui.navDrawer.accounts.AccountModel
import com.ducktapedapps.updoot.ui.navDrawer.destinations.NavDrawerItemModel
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.SingleLiveEvent
import com.ducktapedapps.updoot.utils.accountManagement.IRedditClient
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ActivityVM(private val redditClient: IRedditClient, private val subredditDAO: SubredditDAO) : ViewModel() {
    private val _shouldReload: MutableLiveData<SingleLiveEvent<Boolean>> = MutableLiveData(SingleLiveEvent(false))
    val shouldReload: LiveData<SingleLiveEvent<Boolean>> = _shouldReload

    private val _accounts: MutableLiveData<List<AccountModel>> = MutableLiveData(listOf())
    private val accountEntriesExpanded = MutableLiveData(false)

    private val _navDrawerVisible = MutableLiveData<Float>(2f)
    val navDrawerVisibility = _navDrawerVisible

    val loginState: LiveData<LoginState> = Transformations.map(_accounts) {
        if (it.first().name == Constants.ANON_USER) LoggedOut
        else LoggedIn(it.first().name)
    }

    val subredditSubscription: LiveData<List<Subreddit>> = Transformations.switchMap(loginState) { user ->
        subredditDAO.observeSubscribedSubredditsFor(
                when (user) {
                    is LoggedOut -> ""
                    is LoggedIn -> user.name
                }
        )
    }

    val accounts = MediatorLiveData<List<AccountModel>>().apply {
        var isExpanded: Boolean
        addSource(accountEntriesExpanded) {
            isExpanded = it
            value = if (isExpanded) _accounts.value?.toList()!!
            else listOf(_accounts.value?.first()!!)
        }
        addSource(_accounts) {
            accountEntriesExpanded.value = false
        }
    }

    val navigationEntries: LiveData<List<NavDrawerItemModel>> = Transformations.map(loginState) { account ->
        mutableListOf<NavDrawerItemModel>().apply {
            add(NavDrawerItemModel("Explore", R.drawable.ic_explore_24dp))
            if (account is LoggedIn) {
                add(NavDrawerItemModel("Create Post", R.drawable.ic_baseline_edit_24))
                add(NavDrawerItemModel("Inbox", R.drawable.ic_baseline_inbox_24))
                add(NavDrawerItemModel("Profile", R.drawable.ic_account_circle_24dp))
            }
            add(NavDrawerItemModel("History", R.drawable.ic_baseline_history_24))
        }
    }

    init {
        reloadAccountList()
    }

    private fun reloadAccountList() {
        _accounts.value = redditClient.getAccountModels()
    }

    fun setCurrentAccount(name: String) {
        redditClient.setCurrentAccount(name)
        _shouldReload.value = SingleLiveEvent(true)
        reloadAccountList()
    }

    fun reloadContent() {
        _shouldReload.postValue(SingleLiveEvent(true))
        reloadAccountList()
    }

    fun logout(accountName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val accountRemovedSuccessfully = redditClient.removeUser(accountName)
            if (accountRemovedSuccessfully) withContext(Dispatchers.Main) {
                reloadContent()
            }
        }
    }

    fun expandOrCollapseAccountsMenu() {
        accountEntriesExpanded.value = accountEntriesExpanded.value?.run { !this }
    }

    fun setBottomNavDrawerVisibilityRatio(@FloatRange(from = 0.0, to = 1.0) ratio: Float) {
        viewModelScope.launch(Dispatchers.Main) { _navDrawerVisible.value = ratio }
    }
}

sealed class LoginState {
    object LoggedOut : LoginState()
    data class LoggedIn(val name: String) : LoginState()
}

class ActivityVMFactory @Inject constructor(
        private val redditClient: RedditClient,
        private val subredditDAO: SubredditDAO
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = ActivityVM(redditClient, subredditDAO) as T
}