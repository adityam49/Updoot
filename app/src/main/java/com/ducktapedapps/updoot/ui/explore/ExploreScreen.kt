package com.ducktapedapps.updoot.ui.explore

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.data.local.SubredditDAO
import com.ducktapedapps.updoot.data.local.model.Subreddit
import com.ducktapedapps.updoot.ui.theme.surfaceOnDrawer
import com.ducktapedapps.updoot.utils.accountManagement.IRedditClient
import dev.chrisbanes.accompanist.glide.GlideImage
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
@Composable
fun ExploreScreen(
        onClickSubreddit: (String) -> Unit,
        redditClient: IRedditClient,
        subredditDAO: SubredditDAO
) {
    val coroutineScope = rememberCoroutineScope()
    val exploreVM = remember { ExploreVM(ExploreRepo(redditClient, subredditDAO), coroutineScope) }
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "Trending subreddits", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.overline)
        if (exploreVM.isLoading.collectAsState(true).value)
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        else {
            ScrollableRow {
                exploreVM.trendingSubs
                        .collectAsState(initial = emptyList())
                        .value
                        .forEach {
                            TrendingSub(subreddit = it, onClickSubreddit)
                        }
            }
        }
    }
}

@Composable
fun TrendingSub(subreddit: Subreddit, onClickSubreddit: (String) -> Unit) {
    val resources = ContextAmbient.current.resources
    val displayMetrics = resources.displayMetrics
    val screenWidth = displayMetrics.widthPixels / displayMetrics.density
    Card(
            modifier = Modifier
                    .preferredWidth((screenWidth / 1.2).dp)
                    .wrapContentHeight()
                    .padding(start = 8.dp, end = 8.dp)
                    .clickable(onClick = { onClickSubreddit(subreddit.display_name) }),
            shape = RoundedCornerShape(16.dp),
            backgroundColor = surfaceOnDrawer
    ) {
        Row(modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent)
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            GlideImage(
                    data = subreddit.community_icon,
                    modifier = Modifier.size(48.dp),
                    requestBuilder = {
                        apply(RequestOptions().transform(CenterCrop(), CircleCrop()))
                    },
                    error = {
                        Image(asset = vectorResource(id = R.drawable.ic_subreddit_default_24dp), modifier = Modifier.align(Alignment.CenterVertically))
                    }
            )
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(text = subreddit.display_name, style = MaterialTheme.typography.h6)
                subreddit.public_description?.run {
                    Text(text = this, style = MaterialTheme.typography.caption)
                }
            }
        }
    }
}


@Preview
@Composable
fun PreviewTrendingSub() {
    val subreddit = Subreddit(
            display_name = "r/Android",
            community_icon = "https://b.thumbs.redditmedia.com/EndDxMGB-FTZ2MGtjepQ06cQEkZw_YQAsOUudpb9nSQ.png",
            subscribers = 0,
            accounts_active = 0,
            public_description = "Some info about subreddit",
            description = "Some info about subreddit",
            created = 12334323432,
            lastUpdated = 12334323432,
            isTrending = 0
    )
    TrendingSub(subreddit = subreddit, onClickSubreddit = {})
}