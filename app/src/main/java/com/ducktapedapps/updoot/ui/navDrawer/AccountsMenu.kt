package com.ducktapedapps.updoot.ui.navDrawer

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.loadVectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.ui.theme.SurfaceOnDrawer
import com.ducktapedapps.updoot.ui.theme.UpdootTheme
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel.AnonymousAccount
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel.UserModel
import dev.chrisbanes.accompanist.glide.GlideImage

@Preview
@Composable
fun PreviewAccountsMenuDark() {
    val accounts = listOf(
            AnonymousAccount(true),
            UserModel("Someusername", false, ""),
    )
    UpdootTheme(isDarkTheme = true) {
        AccountsMenu(accounts = accounts, removeAccount = {}, switch = {})
    }
}


@Preview
@Composable
fun PreviewAccountsMenu() {
    val accounts = listOf(
            AnonymousAccount(true),
            UserModel("Someusername", false, ""),
    )
    AccountsMenu(accounts = accounts, removeAccount = {}, switch = {})
}

@Composable
fun AccountsMenu(
        accounts: List<AccountModel>,
        removeAccount: (accountName: String) -> Unit,
        switch: (accountName: String) -> Unit
) {
    val showAllAccounts = remember { mutableStateOf(false) }
    Card(
            modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(8.dp)
                    .animateContentSize(),
            backgroundColor = MaterialTheme.colors.SurfaceOnDrawer,
            shape = RoundedCornerShape(percent = if (accounts.size == 1) 50 else 10),
    ) {
        Column {
            accounts.forEach { accountModel ->
                if (accountModel.isCurrent) CurrentAccountItem(
                        accountModel = accountModel,
                        removeAccount = removeAccount,
                        toggleAccountMenu = { showAllAccounts.value = !showAllAccounts.value }
                )
                else NonCurrentAccountItem(
                        accountModel = accountModel,
                        removeAccount = removeAccount,
                        switch = switch
                )
            }
        }
    }
}


@Composable
fun NonCurrentAccountItem(
        accountModel: AccountModel,
        removeAccount: (accountName: String) -> Unit,
        switch: (accountName: String) -> Unit
) {
    Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { switch(accountModel.name) }),
    ) {
        val paddingModifier = Modifier.padding(top = 16.dp, bottom = 16.dp, start = 20.dp)
        when (accountModel) {
            is AnonymousAccount -> loadVectorResource(id = R.drawable.ic_account_circle_24dp).resource.resource?.let {
                Image(
                        imageVector = it,
                        modifier = paddingModifier
                )
            }
            is UserModel ->
                GlideImage(
                        data = accountModel.userIcon,
                        modifier = paddingModifier.size(24.dp),
                        requestBuilder = {
                            val options = RequestOptions()
                            options.transform(CenterCrop(), CircleCrop())
                            apply(options)
                        }
                )
        }

        Providers(AmbientContentAlpha provides ContentAlpha.high) {
            Text(text = accountModel.name, modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f))
        }

        if (accountModel is UserModel) {
            IconButton(
                    modifier = Modifier.padding(end = 4.dp),
                    onClick = { removeAccount(accountModel.name) }
            ) {
                loadVectorResource(id = R.drawable.ic_baseline_remove_24).resource.resource?.let {
                    Icon(imageVector = it)
                }
            }
        }
    }
}

@Composable
fun CurrentAccountItem(
        accountModel: AccountModel,
        removeAccount: (accountName: String) -> Unit,
        toggleAccountMenu: () -> Unit
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
        if (accountModel is UserModel) GlideImage(
                data = accountModel.userIcon,
                modifier = userIconModifier,
                requestBuilder = {
                    val options = RequestOptions()
                    options.transform(CenterCrop(), CircleCrop())
                    apply(options)
                }
        )
        else loadVectorResource(id = R.drawable.ic_account_circle_24dp).resource.resource?.let {
            Image(
                    imageVector = it,
                    modifier = userIconModifier
            )
        }

        Providers(AmbientContentAlpha provides ContentAlpha.high) {
            Text(text = accountModel.name, modifier = Modifier.weight(1f))
        }
        if (accountModel is UserModel)
            IconButton(
                    onClick = { removeAccount(accountModel.name) },
            ) {
                loadVectorResource(id = R.drawable.ic_baseline_remove_24).resource.resource?.let {
                    Icon(imageVector = it)
                }
            }

        IconButton(
                onClick = { toggleAccountMenu() },
        ) {
            loadVectorResource(id = R.drawable.ic_baseline_chevron_down_24).resource.resource?.let {
                Icon(imageVector = it)
            }
        }
    }
}