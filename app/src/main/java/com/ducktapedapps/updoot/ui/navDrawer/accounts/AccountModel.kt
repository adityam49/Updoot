package com.ducktapedapps.updoot.ui.navDrawer.accounts

import androidx.annotation.DrawableRes

sealed class AccountModel(val name: String) {
    data class SystemModel(
            private val _name: String,
            @DrawableRes val icon: Int
    ) : AccountModel(_name)

    data class UserModel(
            private val _name: String,
            val userIcon: String
    ) : AccountModel(_name)
}
