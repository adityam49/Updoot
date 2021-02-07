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

@Composable
fun NavigationMenu(
        navDestinations: List<NavigationDestination>,
        navigateTo: (NavigationDestination) -> Unit,
) {
    LazyColumn {
        items(navDestinations) { destination ->
            Row(
                    modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = { navigateTo(destination) }),
                    verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                        imageVector = vectorResource(id = destination.icon),
                        contentDescription = "${destination.title} Icon",
                        modifier = Modifier.padding(start = 28.dp, top = 16.dp, bottom = 16.dp),
                )
                Providers(AmbientContentAlpha provides ContentAlpha.high) {
                    Text(text = destination.title, modifier = Modifier.padding(start = 16.dp))
                }
            }
        }
    }
}

@Composable
fun NavigationMenuScreen(viewModel: ActivityVM) {
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
                navigateTo = viewModel::navigateTo,
        )
    }
}
