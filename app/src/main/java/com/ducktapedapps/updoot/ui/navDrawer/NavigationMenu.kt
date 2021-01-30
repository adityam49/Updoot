package com.ducktapedapps.updoot.ui.navDrawer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    LazyColumn {
        items(navDestinations) { item ->
            Row(
                    modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = {
                                when (item) {
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
                        imageVector = vectorResource(id = item.icon),
                        contentDescription = "${item.title} Icon",
                        modifier = Modifier.padding(start = 28.dp, top = 16.dp, bottom = 16.dp),
                )
                Providers(AmbientContentAlpha provides ContentAlpha.high) {
                    Text(text = item.title, modifier = Modifier.padding(start = 16.dp))
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
