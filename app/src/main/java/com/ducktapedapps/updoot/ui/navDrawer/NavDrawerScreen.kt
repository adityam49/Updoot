package com.ducktapedapps.updoot.ui.navDrawer


import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.EmphasisAmbient
import androidx.compose.material.IconButton
import androidx.compose.material.ProvideEmphasis
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.ui.ActivityVM
import com.ducktapedapps.updoot.ui.navDrawer.AccountModel.*
import com.ducktapedapps.updoot.ui.navDrawer.NavigationDestination.Exit
import com.ducktapedapps.updoot.ui.navDrawer.NavigationDestination.Explore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
@Composable
fun NavDrawerScreen(
        viewModel: ActivityVM,
        onLogin: () -> Unit,
        onRemoveAccount: (accountName: String) -> Unit,
        onToggleAccountMenu: () -> Unit,
        onSwitchAccount: (accountName: String) -> Unit,
        onExplore: () -> Unit,
        onExit: () -> Unit
) {
    val accountsList: List<AccountModel> by viewModel.accounts.collectAsState(emptyList())
    val navDestinations: List<NavigationDestination> by viewModel.navigationEntries.collectAsState(emptyList())
    Column(modifier = Modifier.padding(8.dp)) {
        AccountsMenu(
                accounts = accountsList,
                login = onLogin,
                removeAccount = onRemoveAccount,
                toggleAccountMenu = onToggleAccountMenu,
                switch = onSwitchAccount
        )
        NavigationMenu(
                navDestinations = navDestinations,
                onOpenExplore = onExplore,
                onExitApp = onExit
        )
    }
}

@Composable
fun AccountsMenu(
        accounts: List<AccountModel>,
        login: () -> Unit,
        toggleAccountMenu: () -> Unit,
        removeAccount: (accountName: String) -> Unit,
        switch: (accountName: String) -> Unit
) {
    Card(
            modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .animateContentSize()
                    .padding(8.dp),
            elevation = 8.dp,
            shape = RoundedCornerShape(16.dp)
    ) {
        LazyColumnFor(items = accounts, modifier = Modifier.fillMaxWidth().padding(8.dp)) { accountModel: AccountModel ->
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

@Composable
fun NonCurrentAccountItem(
        accountModel: AccountModel,
        login: () -> Unit,
        removeAccount: (accountName: String) -> Unit,
        switch: (accountName: String) -> Unit
) {
    ConstraintLayout(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(onClick = {
                when (accountModel) {
                    AddAccount -> login()
                    is AnonymousAccount, is UserModel -> switch(accountModel.name)
                }
            })
    ) {
        val (icon, name, removeButton) = createRefs()
        IconButton(icon = {
            Icon(asset = when (accountModel) {
                AddAccount -> vectorResource(id = R.drawable.ic_round_add_circle_24)
                is AnonymousAccount -> vectorResource(id = R.drawable.ic_account_circle_24dp)
                is UserModel -> {
                    //TODO : use accountModel.icon here using glide or something
                    vectorResource(id = R.drawable.ic_account_circle_24dp)
                }
            })
        }, modifier = Modifier.constrainAs(icon) {
            start.linkTo(parent.start)
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
        }, onClick = {})
        ProvideEmphasis(emphasis = EmphasisAmbient.current.medium) {
            Text(text = accountModel.name, modifier = Modifier.constrainAs(name) {
                start.linkTo(icon.end, margin = 8.dp)
                top.linkTo(icon.top)
                bottom.linkTo(icon.bottom)
            })
        }
        if (accountModel is UserModel)
            IconButton(
                    onClick = { removeAccount(accountModel.name) },
                    icon = { Icon(asset = vectorResource(id = R.drawable.ic_baseline_remove_24)) },
                    modifier = Modifier.constrainAs(removeButton) {
                        end.linkTo(parent.end)
                    }
            )
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
            .wrapContentHeight()
    ) {
        val (icon, username, removeButton, expandChevron) = createRefs()
        Icon(
                asset = vectorResource(id = R.drawable.ic_account_circle_24dp)
                        .copy(defaultHeight = 48.dp, defaultWidth = 48.dp),
                modifier = Modifier.constrainAs(icon) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
        )
        ProvideEmphasis(emphasis = EmphasisAmbient.current.high) {
            Text(text = accountModel.name, modifier = Modifier.constrainAs(username) {
                start.linkTo(icon.end, margin = 8.dp)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            })
        }
        if (accountModel is UserModel) IconButton(
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

@Composable
fun NavigationMenu(
        navDestinations: List<NavigationDestination>,
        onExitApp: () -> Unit,
        onOpenExplore: () -> Unit
) {
        LazyColumnFor(items = navDestinations) { navDestination ->
            Row(
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 28.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                            .clickable(onClick = {
                                when (navDestination) {
                                    Explore -> onOpenExplore()
                                    Exit -> onExitApp()
                                    else -> Unit
                                }
                            }),
                    verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(asset = vectorResource(id = navDestination.icon), modifier = Modifier.padding(end = 16.dp))
                ProvideEmphasis(emphasis = EmphasisAmbient.current.high) {
                    Text(text = navDestination.title, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

}