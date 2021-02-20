package com.ducktapedapps.updoot.ui.user

import com.ducktapedapps.updoot.utils.accountManagement.AccountModel.AnonymousAccount
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel.UserModel
import com.ducktapedapps.updoot.utils.accountManagement.UpdootAccountsProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface GetUserSectionsUseCase {
    fun getUserSections(userName: String): Flow<List<UserSection>>
}

class GetUserSectionsUseCaseImpl @Inject constructor(
    private val updootAccountsProvider: UpdootAccountsProvider,
) : GetUserSectionsUseCase {

    override fun getUserSections(userName: String): Flow<List<UserSection>> =
        updootAccountsProvider
            .allAccounts
            .map { allAccounts ->
                allAccounts.first { it.isCurrent }
            }.map { currentLoggedInAccount ->
                when (currentLoggedInAccount) {
                    is UserModel -> {
                        if (userName == currentLoggedInAccount.name) getAllUserSections()
                        else getNonUserSpecificSections()
                    }
                    is AnonymousAccount -> getNonUserSpecificSections()
                }
            }
}