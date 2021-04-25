package com.ducktapedapps.updoot.navDrawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.transform.CircleCropTransformation
import com.ducktapedapps.updoot.ActivityVM
import com.ducktapedapps.updoot.R
import com.google.accompanist.coil.CoilImage
import com.google.accompanist.imageloading.ImageLoadState.Success
import kotlinx.coroutines.flow.map

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
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
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
    val trendingSubreddits = viewModel.trending.collectAsState()
    val multiReddits = viewModel
        .userMultiRedditSubscription
        .map { allMultiReddits ->
            //TODO : hoist expanded state to viewModel???
            allMultiReddits.map { Pair(it, mutableStateOf(false)) }
        }
        .collectAsState(initial = emptyList())

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


        multiReddits.value.forEach {
            stickyHeader(it.first.multiRedditName) {
                SubscriptionGroupHeaders(
                    iconName = it.first.multiRedditIcon,
                    groupName = it.first.multiRedditName,
                    toggleExpand = { it.second.value = !it.second.value },
                    onClickAction = {},
                    isExpanded = it.second.value,
                )
            }
            if (it.second.value) items(it.first.subreddits) { subreddit ->
                Row {
                    Spacer(modifier = Modifier.padding(16.dp))
                    SubredditItem(subreddit = subreddit, openSubreddit)
                }
            }
        }
        subscriptions.value.run {
            if (this.isNotEmpty()) {
                stickyHeader {
                    SubscriptionGroupHeaders(
                        iconName = "",
                        groupName = "Subscriptions",
                        onClickAction = { },
                        toggleExpand = { },
                        isExpanded = true,
                    )
                }
                items(this) { subscribedSubreddit ->
                    Row {
                        Spacer(modifier = Modifier.padding(16.dp))
                        SubredditItem(subreddit = subscribedSubreddit, openSubreddit)
                    }
                }
            } else {
                stickyHeader {
                    SubscriptionGroupHeaders(
                        iconName = "",
                        groupName = "Subreddits Trending Today",
                        toggleExpand = {},
                        onClickAction = {},
                        isExpanded = true
                    )
                }
                items(trendingSubreddits.value) { trendingSubreddit ->
                    Row {
                        Spacer(modifier = Modifier.padding(16.dp))
                        SubredditItem(subreddit = trendingSubreddit, openSubreddit)
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionGroupHeaders(
    iconName: String,
    groupName: String,
    isExpanded: Boolean,
    onClickAction: () -> Unit = {},
    toggleExpand: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(onClick = onClickAction),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoilImage(
            data = iconName,
            requestBuilder = {
                transformations(CircleCropTransformation())
            },
            modifier = Modifier
                .padding(start = 28.dp, end = 32.dp, top = 8.dp, bottom = 8.dp)
                .size(32.dp)
        ) {
            when (it) {
                is Success -> Image(it.painter, stringResource(id = R.string.subreddit_icon))
                else -> Icon(
                    painterResource(id = R.drawable.ic_subreddit_default_24dp),
                    stringResource(id = R.string.subreddit_icon)
                )
            }
        }
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
            Text(text = groupName, style = MaterialTheme.typography.body1)
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = toggleExpand) {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = Icons.Default.ArrowDropDown.name,
                modifier = Modifier.rotate(if (isExpanded) 180f else 0f)
            )
        }
    }
}

@Composable
fun Header(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Divider(modifier = Modifier.weight(1f))
        Text(
            modifier = Modifier.padding(start = 28.dp, top = 16.dp, bottom = 16.dp, end = 16.dp),
            text = title,
            style = MaterialTheme.typography.overline
        )
        Divider(modifier = Modifier.weight(1f))
    }
}
