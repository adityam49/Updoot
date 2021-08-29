package com.ducktapedapps.updoot.navDrawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.data.local.model.LocalSubreddit
import com.ducktapedapps.updoot.utils.getCompactAge
import java.util.*

@Composable
fun SubredditItem(subreddit: SubscriptionSubredditUiModel, openSubreddit: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openSubreddit(subreddit.subredditName) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = rememberImagePainter(data=subreddit.icon){
                error(R.drawable.ic_subreddit_default_24dp)
                transformations(CircleCropTransformation())
            },
            contentDescription = stringResource(R.string.subreddit_icon),
            modifier = Modifier
                .padding(start = 28.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                .size(32.dp)
        )
        Column {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                Text(text = subreddit.subredditName, style = MaterialTheme.typography.body1)
            }
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled) {
                Text(
                    text = getCompactAge(subreddit.created.time),
                    style = MaterialTheme.typography.caption
                )
            }
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