package com.ducktapedapps.updoot.subreddit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.common.PageLoading
import com.ducktapedapps.updoot.common.PageLoadingFailed
import com.ducktapedapps.updoot.subreddit.SubredditInfoState.Loading
import com.ducktapedapps.updoot.subreddit.SubredditInfoState.LoadingFailed
import com.ducktapedapps.updoot.subreddit.SubredditInfoState.UiModel
import com.ducktapedapps.updoot.utils.getCompactAge
import com.ducktapedapps.updoot.utils.getCompactCountAsString
import java.util.Date


/**
 *  Subreddit sidebar UI component
 */
@Composable
fun SubredditInfo(
    viewState: ViewState,
    doAction: (ScreenAction) -> Unit,
) {
    when (val data = viewState.subredditInfo) {
        Loading -> PageLoading()
        is UiModel -> {

            SubredditInfoHeader(
                modifier = Modifier.fillMaxWidth(),
                iconUrl = data.icon,
                activeMembers = data.activeAccounts,
                subscribers = data.subscribers,
                created = data.created,
                isSubscribed = null,
                toggleSubscription = { doAction(ScreenAction.ToggleSubredditSubscription) },
            )

            Info(
                description = data.info,
                modifier = Modifier.fillMaxWidth()
            )
        }

        is LoadingFailed -> PageLoadingFailed(
            performRetry = { doAction(ScreenAction.LoadSubredditInfo) },
            message = data.reason
        )

        null -> Box(Modifier.size(0.dp))
    }
}

@Composable
private fun SubredditInfoHeader(
    modifier: Modifier,
    iconUrl: String?,
    activeMembers: Long?,
    subscribers: Long?,
    created: Date,
    isSubscribed: Boolean?,
    toggleSubscription: () -> Unit,
) {
    Row(modifier = modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = iconUrl,
            error = painterResource(id = R.drawable.ic_subreddit_default_24dp),
            contentDescription = stringResource(id = R.string.subreddit_icon),
            modifier = Modifier
                .size(48.dp)
                .clip(shape = CircleShape),
        )
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = (subscribers?.run { getCompactCountAsString(this) + " Subscribers " }
                ?: "") +
                    (activeMembers?.run { " • " + getCompactCountAsString(this) + " active " }
                        ?: "") +
                    created.run { "\n" + getCompactAge(time) },
            style = MaterialTheme.typography.labelSmall
        )
        Button(
            onClick = { if (isSubscribed != null) toggleSubscription() },
            enabled = isSubscribed != null
        ) {
            Text(
                text = if (isSubscribed == true) stringResource(R.string.leave)
                else stringResource(R.string.join)
            )
        }
    }
}

@Composable
private fun Info(modifier: Modifier, description: String?) {
    Text(
        modifier = modifier,
        text = description ?: "",
        style = MaterialTheme.typography.bodyMedium
    )
}