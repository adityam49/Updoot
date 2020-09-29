package com.ducktapedapps.updoot.ui.navDrawer

import androidx.annotation.DrawableRes
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.utils.Constants

sealed class AccountModel(val name: String, open val isCurrent: Boolean) {
    object AddAccount : AccountModel(Constants.ADD_ACCOUNT, false) {
        @DrawableRes
        val icon = R.drawable.ic_round_add_circle_24
    }

    data class AnonymousAccount(
            override val isCurrent: Boolean
    ) : AccountModel(Constants.ANON_USER, isCurrent) {
        @DrawableRes
        val icon = R.drawable.ic_account_circle_24dp
    }

    data class UserModel(
            private val _name: String,
            override val isCurrent: Boolean,
            val userIcon: String
    ) : AccountModel(_name, isCurrent)
}