package com.ducktapedapps.updoot.subscriptions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ducktapedapps.navigation.Event
import com.ducktapedapps.navigation.NavigationDirections
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.data.local.model.LocalSubreddit
import com.ducktapedapps.updoot.utils.getCompactAge
import java.util.*

@Composable
fun SubscriptionsScreen(
    publishEvent: (Event) -> Unit,
) {
    val viewModel: SubscriptionsVM = hiltViewModel<SubscriptionsVMImpl>()
    val viewState = viewModel.viewState.collectAsState()
    LazyColumn {
        stickyHeader { SubscriptionsAppBar(viewModel::syncSubscriptions) }
        items(viewState.value.subscriptions) { subscription ->
            SubredditItem(
                subreddit = subscription,
                openSubreddit = {
                    publishEvent(
                        Event.ScreenNavigationEvent(
                            NavigationDirections.SubredditScreenNavigation.open(
                                subredditName = it
                            )
                        )
                    )
                }
            )
        }
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun SubscriptionsAppBar(syncSubscriptions: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = stringResource(id = R.string.subscriptions))
            IconButton(onClick = syncSubscriptions) {
                Icon(Icons.Outlined.Refresh, Icons.Outlined.Refresh.name)
            }
        }
    }
}

@Composable
fun SubredditItem(subreddit: SubscriptionSubredditUiModel, openSubreddit: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openSubreddit(subreddit.subredditName) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = subreddit.icon,
            error = painterResource(R.drawable.ic_subreddit_default_24dp),
            contentDescription = stringResource(R.string.subreddit_icon),
            modifier = Modifier
                .padding(start = 28.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                .size(32.dp)
                .clip(shape = CircleShape),
        )
        Column {
            Text(text = subreddit.subredditName, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = getCompactAge(subreddit.created.time),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

data class SubscriptionSubredditUiModel(
    val icon: String? = null,
    val subredditName: String,
    val created: Date,
)

data class MultiRedditUiModel(
    val multiRedditName: String,
    val multiRedditIcon: String,
    val subreddits: List<SubscriptionSubredditUiModel>
)

fun LocalSubreddit.toSubscriptionSubredditUiModel(): SubscriptionSubredditUiModel =
    SubscriptionSubredditUiModel(
        icon = icon,
        subredditName = subredditName,
        created = created
    )