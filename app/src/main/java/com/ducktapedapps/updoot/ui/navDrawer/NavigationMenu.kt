package com.ducktapedapps.updoot.ui.navDrawer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.AmbientContentAlpha
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.ducktapedapps.updoot.ui.ActivityVM
import com.ducktapedapps.updoot.ui.navDrawer.NavigationDestination.*

@Composable
fun NavigationMenu(
        navDestinations: List<NavigationDestination>,
        onExitApp: () -> Unit,
        onOpenExplore: () -> Unit,
        onSearch: () -> Unit,
        openSettings: () -> Unit,
        addAccount: () -> Unit,
) {
    Column {
        navDestinations.forEach { navDestination ->
            Row(
                    modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = {
                                when (navDestination) {
                                    Explore -> onOpenExplore()
                                    Exit -> onExitApp()
                                    Search -> onSearch()
                                    AddAccount -> addAccount()
                                    Settings -> openSettings()
                                    else -> Unit
                                }
                            }),
                    verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                        vectorResource(id = navDestination.icon),
                        modifier = Modifier.padding(start = 28.dp, top = 16.dp, bottom = 16.dp)
                )
                Providers(AmbientContentAlpha provides ContentAlpha.high) {
                    Text(text = navDestination.title, modifier = Modifier.padding(start = 16.dp))
                }
            }
        }
    }
}

@Composable
fun NavigationMenuScreen(
        viewModel: ActivityVM,
        onExplore: () -> Unit,
        onSearch: () -> Unit,
        onExit: () -> Unit,
        onAddAccount: () -> Unit,
        onOpenSettings: () -> Unit
) {
    val accountsList = viewModel.accounts.collectAsState()
    val navDestinations = viewModel.navigationEntries.collectAsState()
    Column(modifier = Modifier.fillMaxSize()) {
        AccountsMenu(
                accounts = accountsList.value,
                removeAccount = viewModel::logout,
                switch = viewModel::setCurrentAccount
        )
        NavigationMenu(
                navDestinations = navDestinations.value,
                onOpenExplore = onExplore,
                onSearch = onSearch,
                onExitApp = onExit,
                addAccount = onAddAccount,
                openSettings = onOpenSettings,
        )
    }
}
