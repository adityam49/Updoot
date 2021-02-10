package com.ducktapedapps.updoot.ui.explore

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.res.loadVectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.data.local.SubredditDAO
import com.ducktapedapps.updoot.data.local.model.LocalSubreddit
import com.ducktapedapps.updoot.ui.theme.SurfaceOnDrawer
import com.ducktapedapps.updoot.utils.accountManagement.IRedditClient
import dev.chrisbanes.accompanist.glide.GlideImage
import dev.chrisbanes.accompanist.imageloading.ImageLoadState
import java.util.*


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
            rememberScrollState(0f)
            LazyRow {
                item {
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
}

@Composable
fun TrendingSub(subreddit: LocalSubreddit, onClickSubreddit: (String) -> Unit) {
    val resources = AmbientContext.current.resources
    val displayMetrics = resources.displayMetrics
    val screenWidth = displayMetrics.widthPixels / displayMetrics.density
    Card(
            modifier = Modifier
                    .preferredWidth((screenWidth / 1.2).dp)
                    .wrapContentHeight()
                    .padding(start = 8.dp, end = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = { onClickSubreddit(subreddit.subredditName) }),
            backgroundColor = MaterialTheme.colors.SurfaceOnDrawer,
    ) {
        Row(
                modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
        ) {
            GlideImage(
                    data = subreddit.icon,
                    modifier = Modifier
                            .size(48.dp)
                            .padding(start = 16.dp, top = 16.dp),
                    requestBuilder = {
                        apply(RequestOptions().transform(CenterCrop(), CircleCrop()))
                    }
            ) { imageLoadState ->
                if (imageLoadState is ImageLoadState.Error)
                    loadVectorResource(id = R.drawable.ic_subreddit_default_24dp)
                            .resource.resource?.let {
                                Image(
                                        imageVector = it,
                                        contentDescription = "subreddit Icon",
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
                            modifier = Modifier.padding(16.dp))
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