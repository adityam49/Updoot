package com.ducktapedapps.updoot.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.ui.navDrawer.accounts.AccountModel
import com.ducktapedapps.updoot.utils.Constants
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
    val accounts: LiveData<List<AccountModel>> = _accounts

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
}