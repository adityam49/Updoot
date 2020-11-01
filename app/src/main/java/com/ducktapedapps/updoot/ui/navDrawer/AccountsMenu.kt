package com.ducktapedapps.updoot.ui.navDrawer

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.ui.theme.UpdootTheme
import com.ducktapedapps.updoot.ui.theme.surfaceOnDrawer
import dev.chrisbanes.accompanist.glide.GlideImage

@Preview
@Composable
fun PreviewAccountsMenuDark() {
    val accounts = listOf(
            AccountModel.AnonymousAccount(true),
            AccountModel.UserModel("Someusername", false, ""),
            AccountModel.AddAccount
    )
    UpdootTheme(isDarkTheme = true) {
        AccountsMenu(accounts = accounts, login = {}, toggleAccountMenu = {}, removeAccount = {}, switch = {})
    }
}


@Preview
@Composable
fun PreviewAccountsMenu() {
    val accounts = listOf(
            AccountModel.AnonymousAccount(true),
            AccountModel.UserModel("Someusername", false, ""),
            AccountModel.AddAccount
    )
    AccountsMenu(accounts = accounts, login = {}, toggleAccountMenu = {}, removeAccount = {}, switch = {})
}

@Composable
fun AccountsMenu(
        accounts: List<AccountModel>,
        login: () -> Unit,
        toggleAccountMenu: () -> Unit,
        removeAccount: (accountName: String) -> Unit,
        switch: (accountName: String) -> Unit
) {
    Surface(
            modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(8.dp)
                    .animateContentSize(),
            shape = RoundedCornerShape(if (accounts.size == 1) 32.dp else 16.dp),
            color = surfaceOnDrawer,
            elevation = 1.dp
    ) {
        Column {
            accounts.forEach { accountModel ->
                if (accountModel.isCurrent) CurrentAccountItem(
                        accountModel = accountModel,
                        removeAccount = removeAccount,
                        toggleAccountMenu = toggleAccountMenu
                )
                else NonCurrentAccountItem(
                        accountModel = accountModel,
                        login = login,
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
        login: () -> Unit,
        removeAccount: (accountName: String) -> Unit,
        switch: (accountName: String) -> Unit
) {
    Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = {
                        when (accountModel) {
                            AccountModel.AddAccount -> login()
                            is AccountModel.AnonymousAccount, is AccountModel.UserModel -> switch(accountModel.name)
                        }
                    }),


            ) {
        val paddingModifier = Modifier.padding(top = 16.dp, bottom = 16.dp, start = 20.dp)
        when (accountModel) {
            AccountModel.AddAccount -> Image(
                    modifier = paddingModifier,
                    asset = vectorResource(id = R.drawable.ic_round_add_circle_24)
            )
            is AccountModel.AnonymousAccount -> Image(
                    modifier = paddingModifier,
                    asset = vectorResource(id = R.drawable.ic_account_circle_24dp)
            )
            is AccountModel.UserModel ->
                // This will automatically use the value of AmbientRequestManager
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

        ProvideEmphasis(emphasis = AmbientEmphasisLevels.current.medium) {
            Text(text = accountModel.name, modifier = Modifier.padding(start = 8.dp))
        }

        if (accountModel is AccountModel.UserModel) {
            IconButton(
                    onClick = { removeAccount(accountModel.name) },
                    icon = { Image(asset = vectorResource(id = R.drawable.ic_baseline_remove_24)) },
            )
        }
    }
}

@Composable
fun CurrentAccountItem(
        accountModel: AccountModel,
        removeAccount: (accountName: String) -> Unit,
        toggleAccountMenu: () -> Unit
) {
    ConstraintLayout(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .wrapContentHeight()
    ) {
        val (icon, username, removeButton, expandChevron) = createRefs()
        val iconModifier = Modifier.constrainAs(icon) {
            start.linkTo(parent.start)
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
        }

        if (accountModel is AccountModel.UserModel) GlideImage(
                data = accountModel.userIcon,
                modifier = iconModifier.size(48.dp),
                requestBuilder = {
                    val options = RequestOptions()
                    options.transform(CenterCrop(), CircleCrop())
                    apply(options)
                }
        )
        else Image(
                asset = vectorResource(id = R.drawable.ic_account_circle_24dp)
                        .copy(defaultHeight = 48.dp, defaultWidth = 48.dp),
                modifier = iconModifier
        )

        ProvideEmphasis(emphasis = AmbientEmphasisLevels.current.high) {
            Text(text = accountModel.name, modifier = Modifier.constrainAs(username) {
                start.linkTo(icon.end, margin = 8.dp)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            })
        }
        if (accountModel is AccountModel.UserModel) IconButton(
                icon = { Icon(asset = vectorResource(id = R.drawable.ic_baseline_remove_24)) },
                onClick = {
                    removeAccount(accountModel.name)
                },
                modifier = Modifier.constrainAs(removeButton) {
                    end.linkTo(expandChevron.start)
                }
        )
        IconButton(
                onClick = { toggleAccountMenu() },
                icon = { Icon(asset = vectorResource(id = R.drawable.ic_baseline_chevron_down_24)) },
                modifier = Modifier.constrainAs(expandChevron) {
                    end.linkTo(parent.end)
                }
        )
    }
}
