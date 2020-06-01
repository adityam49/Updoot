package com.ducktapedapps.updoot.ui.navDrawer.accounts

import androidx.annotation.DrawableRes

sealed class AccountModel(val name: String, val isCurrentAccount: Boolean) {
    data class SystemModel(
            private val _name: String,
            private val _isCurrentAccount: Boolean,
            @DrawableRes val icon: Int
    ) : AccountModel(_name, _isCurrentAccount)

    data class UserModel(
            private val _name: String,
            private val _isCurrentAccount: Boolean,
            val userIcon: String
    ) : AccountModel(_name, _isCurrentAccount)
}
