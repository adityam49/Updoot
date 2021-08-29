package com.ducktapedapps.updoot.navDrawer

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.theme.SurfaceOnDrawer
import com.ducktapedapps.updoot.theme.UpdootTheme
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel.AnonymousAccount
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel.UserModel

@Preview
@Composable
fun PreviewAccountsMenuDark() {
    val accounts = listOf(
        AnonymousAccount(false),
        UserModel("Someusername", true, ""),
    )
    UpdootTheme(isDarkTheme = true) {
        AccountsMenu(accounts = accounts, removeAccount = {}, switch = {}, {})
    }
}


@Preview
@Composable
fun PreviewAccountsMenu() {
    val accounts = listOf(
        AnonymousAccount(false),
        UserModel("Someusername", true, ""),
    )
    AccountsMenu(accounts = accounts, removeAccount = {}, switch = {}, {})
}

@Composable
fun AccountsMenu(
    accounts: List<AccountModel>,
    removeAccount: (accountName: String) -> Unit,
    switch: (accountName: String) -> Unit,
    openAccountInfo: (String) -> Unit
) {
    val showAllAccounts = remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp)
            .animateContentSize(),
        backgroundColor = MaterialTheme.colors.SurfaceOnDrawer,
        shape = RoundedCornerShape(
            percent =
            if (accounts.size != 1 && showAllAccounts.value) 10
            else 50
        ),
    ) {
        Column {
            accounts
                .take(if (showAllAccounts.value) accounts.size else 1)
                .forEach { accountModel ->
                    if (accountModel.isCurrent) CurrentAccountItem(
                        accountModel = accountModel,
                        removeAccount = removeAccount,
                        toggleAccountMenu = { showAllAccounts.value = !showAllAccounts.value },
                        openAccountInfo = { openAccountInfo(accountModel.name) },
                    )
                    else NonCurrentAccountItem(
                        accountModel = accountModel,
                        removeAccount = removeAccount,
                        switch = {
                            showAllAccounts.value = false
                            switch(accountModel.name)
                        },
                        openAccountInfo = { openAccountInfo(accountModel.name) },
                    )
                }
        }
    }
}


@Composable
fun NonCurrentAccountItem(
    accountModel: AccountModel,
    removeAccount: (accountName: String) -> Unit,
    switch: (accountName: String) -> Unit,
    openAccountInfo: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { switch(accountModel.name) }),
    ) {
        val paddingModifier = Modifier.padding(top = 16.dp, bottom = 16.dp, start = 20.dp)
        when (accountModel) {
            is AnonymousAccount ->
                Image(
                    painter = painterResource(id = R.drawable.ic_account_circle_24dp),
                    contentDescription = "Account Icon",
                    modifier = paddingModifier,
                )
            is UserModel ->
            Image(
                painter = rememberImagePainter(data=accountModel.userIcon){
                    fallback(R.drawable.ic_account_circle_24dp)
                    error(R.drawable.ic_account_circle_24dp)
                    transformations(CircleCropTransformation())
                },
                contentDescription ="Account Image" ,
                modifier = paddingModifier.size(24.dp),
            )
        }

        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
            Text(
                text = accountModel.name, modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            )
        }

        if (accountModel is UserModel) {
            IconButton(onClick = openAccountInfo) {
                Icon(imageVector = Icons.Default.Info, contentDescription = "User info Icon")
            }
            IconButton(
                modifier = Modifier.padding(end = 4.dp),
                onClick = { removeAccount(accountModel.name) }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_remove_24),
                    contentDescription = "Remove Account Icon"
                )
            }
        }
    }
}

@Composable
fun CurrentAccountItem(
    accountModel: AccountModel,
    removeAccount: (accountName: String) -> Unit,
    toggleAccountMenu: () -> Unit,
    openAccountInfo: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val userIconModifier = Modifier
            .size(64.dp)
            .padding(8.dp)
        if (accountModel is UserModel)
        Image(
            painter = rememberImagePainter(data=accountModel.userIcon){
                error(R.drawable.ic_account_circle_24dp)
                transformations(CircleCropTransformation())
            },
            contentDescription ="Account Image" ,
                modifier = userIconModifier,
        )
        else
            Image(
                painter = painterResource(id = R.drawable.ic_account_circle_24dp),
                contentDescription = "Account Icon",
                modifier = userIconModifier,
            )

        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
            Text(text = accountModel.name, modifier = Modifier.weight(1f))
        }
        if (accountModel is UserModel) {
            IconButton(onClick = openAccountInfo) {
                Icon(imageVector = Icons.Default.Info, contentDescription = "User info Icon")
            }
            IconButton(
                onClick = { removeAccount(accountModel.name) },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_remove_24),
                    "Account Removal Icon"
                )
            }
        }

        IconButton(
            onClick = { toggleAccountMenu() },
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_chevron_down_24),
                contentDescription = "Chevron"
            )
        }
    }
}