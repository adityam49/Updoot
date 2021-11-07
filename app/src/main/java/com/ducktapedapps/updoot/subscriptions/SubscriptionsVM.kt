package com.ducktapedapps.updoot.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel.AnonymousAccount
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel.UserModel
import com.ducktapedapps.updoot.utils.accountManagement.UpdootAccountsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

interface SubscriptionsVM {
    val viewState: StateFlow<ViewState>

    fun syncSubscriptions()
}

@HiltViewModel
class SubscriptionsVMImpl @Inject constructor(
    private val getUserSubscriptionsUseCase: GetUserSubscriptionsUseCase,
    private val syncUserSubscriptionsUseCase: UpdateUserSubscriptionUseCase,
    private val accountsProvider: UpdootAccountsProvider,
) : SubscriptionsVM, ViewModel() {
    private val currentAccount: StateFlow<AccountModel> = accountsProvider
        .getCurrentAccount()
        .stateIn(viewModelScope, SharingStarted.Eagerly, AnonymousAccount(true))

    override val viewState: StateFlow<ViewState> = getUserSubscriptionsUseCase
        .subscriptions
        .mapLatest { ViewState(it.map { sub -> sub.toSubscriptionSubredditUiModel() }) }
        .stateIn(viewModelScope, SharingStarted.Lazily, ViewState.defaultState())

    override fun syncSubscriptions() {
        viewModelScope.launch {
            when (currentAccount.value) {
                is UserModel -> syncUserSubscriptionsUseCase.updateUserSubscription(currentAccount.value.name)
                is AnonymousAccount -> Unit
            }
        }
    }

}

data class ViewState(
    val subscriptions: List<SubscriptionSubredditUiModel>
) {
    companion object {
        fun defaultState() = ViewState(emptyList())
    }
}