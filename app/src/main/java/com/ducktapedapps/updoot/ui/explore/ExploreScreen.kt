package com.ducktapedapps.updoot.ui.explore

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.transform.CircleCropTransformation
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.data.local.model.LocalSubreddit
import com.google.accompanist.coil.CoilImage
import com.google.accompanist.imageloading.ImageLoadState.Success
import java.util.*


@Composable
fun ExploreScreen(
    onClickSubreddit: (String) -> Unit,
    viewModel: ExploreVM,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Trending subreddits",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.overline
        )
        if (viewModel.isLoading.collectAsState(true).value)
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        else {
            LazyRow {
                item {
                    viewModel.trendingSubs
                        .collectAsState(initial = emptyList())
                        .value
                        .forEach {
                            TrendingSub(subreddit = it, onClickSubreddit)
                        }
                }
            }
        }
    }
}

@Composable
fun TrendingSub(subreddit: LocalSubreddit, onClickSubreddit: (String) -> Unit) {
    val resources = LocalContext.current.resources
    val displayMetrics = resources.displayMetrics
    val screenWidth = displayMetrics.widthPixels / displayMetrics.density
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .requiredWidth((screenWidth / 1.2).dp)
            .wrapContentHeight()
            .padding(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clickable { onClickSubreddit(subreddit.subredditName) },
        ) {
            CoilImage(
                data = subreddit.icon,
                modifier = Modifier
                    .size(48.dp)
                    .padding(start = 16.dp, top = 16.dp),
                requestBuilder = {
                    transformations(CircleCropTransformation())
                }
            ) { imageLoadState ->
                when (imageLoadState) {
                    is Success -> Image(
                        painter = imageLoadState.painter,
                        contentDescription = stringResource(id = R.string.subreddit_icon),
                    )
                    else -> Image(
                        painter = painterResource(id = R.drawable.ic_subreddit_default_24dp),
                        contentDescription = stringResource(id = R.string.subreddit_icon),
                    )

                }
            }
            Column {
                Text(
                    text = subreddit.subredditName,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                )
                subreddit.shortDescription?.run {
                    Text(
                        text = this,
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}


@Preview
@Composable
fun PreviewTrendingSub() {
    val subreddit = LocalSubreddit(
        subredditName = "r/Android",
        icon = "https://b.thumbs.redditmedia.com/EndDxMGB-FTZ2MGtjepQ06cQEkZw_YQAsOUudpb9nSQ.png",
        subscribers = 0,
        accountsActive = 0,
        shortDescription = "Some info about subreddit",
        longDescription = "Some info about subreddit",
        created = Date(12334323432),
        lastUpdated = Date(),
    )
    TrendingSub(subreddit = subreddit, onClickSubreddit = {})
}