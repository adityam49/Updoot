package com.ducktapedapps.updoot.ui.navDrawer

import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AmbientEmphasisLevels
import androidx.compose.material.Icon
import androidx.compose.material.ProvideEmphasis
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.ducktapedapps.updoot.ui.ActivityVM

@Composable
fun NavigationMenu(
        navDestinations: List<NavigationDestination>,
        onExitApp: () -> Unit,
        onOpenExplore: () -> Unit,
        onSearch: () -> Unit,
) {
    Column {
        navDestinations.forEach { navDestination ->
            Row(
                    modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = {
                                when (navDestination) {
                                    NavigationDestination.Explore -> onOpenExplore()
                                    NavigationDestination.Exit -> onExitApp()
                                    NavigationDestination.Search -> onSearch()
                                    else -> Unit
                                }
                            }),
                    verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                        vectorResource(id = navDestination.icon),
                        modifier = Modifier.padding(start = 28.dp, top = 16.dp, bottom = 16.dp)
                )
                ProvideEmphasis(emphasis = AmbientEmphasisLevels.current.high) {
                    Text(text = navDestination.title, modifier = Modifier.padding(start = 16.dp))
                }
            }
        }
    }
}

@Composable
fun NavigationMenuScreen(
        viewModel: ActivityVM,
        onLogin: () -> Unit,
        onRemoveAccount: (accountName: String) -> Unit,
        onToggleAccountMenu: () -> Unit,
        onSwitchAccount: (accountName: String) -> Unit,
        onExplore: () -> Unit,
        onSearch: () -> Unit,
        onExit: () -> Unit
) {
    val accountsList: List<AccountModel> by viewModel.accounts.collectAsState(emptyList())
    val navDestinations: List<NavigationDestination> by viewModel.navigationEntries.collectAsState(emptyList())
    Column {
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
                onSearch = onSearch,
                onExitApp = onExit
        )
    }
}
