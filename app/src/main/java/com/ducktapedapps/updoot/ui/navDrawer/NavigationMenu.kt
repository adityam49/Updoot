package com.ducktapedapps.updoot.ui.navDrawer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ducktapedapps.updoot.ui.ActivityVM

@Composable
fun DestinationItem(
    destination: NavigationDestination,
    navigateTo: (NavigationDestination) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { navigateTo(destination) }),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(destination.icon),
            contentDescription = "${destination.title} Icon",
            modifier = Modifier.padding(start = 28.dp, top = 16.dp, bottom = 16.dp),
        )
        Providers(LocalContentAlpha provides ContentAlpha.high) {
            Text(text = destination.title, modifier = Modifier.padding(start = 16.dp))
        }
    }
}


@Composable
fun NavigationMenuScreen(
    viewModel: ActivityVM,
    openSubreddit: (String) -> Unit,
    openUser: (String) -> Unit
) {
    val accountsList = viewModel.accounts.collectAsState()
    val navDestinations = viewModel.navigationEntries.collectAsState()
    val subscriptions = viewModel.subscriptions.collectAsState()
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        stickyHeader {
            AccountsMenu(
                accounts = accountsList.value,
                removeAccount = viewModel::logout,
                switch = viewModel::setCurrentAccount,
                openAccountInfo = openUser
            )
        }
        items(navDestinations.value) { DestinationItem(it, viewModel::navigateTo) }

        item { Header("Subscriptions") }

        items(subscriptions.value) { SubredditItem(it, openSubreddit) }

    }
}

@Composable
fun Header(title: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            modifier = Modifier.padding(start = 28.dp, top = 16.dp, bottom = 16.dp, end = 16.dp),
            text = title,
            style = MaterialTheme.typography.overline
        )
        Divider()
    }
}
