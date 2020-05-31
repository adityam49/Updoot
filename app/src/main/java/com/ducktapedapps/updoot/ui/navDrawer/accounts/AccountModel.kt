package com.ducktapedapps.updoot.ui.navDrawer.accounts

import androidx.annotation.DrawableRes

data class AccountModel(
        val name: String,
        val isRemovable: Boolean,
        @DrawableRes val icon: Int
)