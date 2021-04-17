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
import coil.transform.CircleCropTransformation
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.data.local.model.LocalSubreddit
import com.ducktapedapps.updoot.utils.getCompactAge
import com.google.accompanist.coil.CoilImage
import com.google.accompanist.imageloading.ImageLoadState.Success
import java.util.*

@Composable
fun SubredditItem(subreddit: SubscriptionSubredditUiModel, openSubreddit: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openSubreddit(subreddit.subredditName) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoilImage(
            data = subreddit.icon ?: "",
            requestBuilder = {
                transformations(CircleCropTransformation())
            },
            modifier = Modifier
                .padding(start = 28.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                .size(32.dp)
        ) { state ->
            when (state) {
                is Success -> Image(
                    painter = state.painter,
                    contentDescription = stringResource(R.string.subreddit_icon)
                )
                else -> Icon(
                    painter = painterResource(id = R.drawable.ic_subreddit_default_24dp),
                    contentDescription = stringResource(id = R.string.subreddit_icon)
                )
            }
        }

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

fun LocalSubreddit.toSubscriptionSubredditUiModel(): SubscriptionSubredditUiModel =
    SubscriptionSubredditUiModel(
        icon = icon,
        subredditName = subredditName,
        created = created
    )