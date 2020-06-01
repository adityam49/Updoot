package com.ducktapedapps.updoot.ui

import androidx.lifecycle.*
import com.ducktapedapps.updoot.ui.navDrawer.accounts.AccountModel
import com.ducktapedapps.updoot.utils.SingleLiveEvent
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ActivityVM @Inject constructor(private val redditClient: RedditClient) : ViewModel() {
    private val _shouldReload: MutableLiveData<SingleLiveEvent<Boolean>> = MutableLiveData(SingleLiveEvent(false))
    val shouldReload: LiveData<SingleLiveEvent<Boolean>> = _shouldReload

    private val _accounts: MutableLiveData<List<AccountModel>> = MutableLiveData(listOf())
    private val accountEntriesExpanded = MutableLiveData(false)

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
}