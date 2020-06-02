package com.ducktapedapps.updoot.ui.navDrawer.accounts

import androidx.annotation.DrawableRes
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.utils.Constants

sealed class AccountModel(val name: String) {
    object AddAccount : AccountModel(Constants.ADD_ACCOUNT) {
        @DrawableRes
        val icon = R.drawable.ic_round_add_circle_24
    }

    data class AnonymousAccount(
            val isCurrent: Boolean
    ) : AccountModel(Constants.ANON_USER) {
        @DrawableRes
        val icon = R.drawable.ic_account_circle_24dp
    }

    data class UserModel(
            private val _name: String,
            val isCurrentAccount: Boolean,
            val userIcon: String
    ) : AccountModel(_name)
}
