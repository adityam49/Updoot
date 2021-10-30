package com.ducktapedapps.updoot.subreddit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ducktapedapps.navigation.Event
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.common.PageEnd
import com.ducktapedapps.updoot.common.PageLoading
import com.ducktapedapps.updoot.common.PageLoadingFailed
import com.ducktapedapps.updoot.utils.PagingModel
import com.ducktapedapps.updoot.utils.PagingModel.Footer.*
import com.ducktapedapps.updoot.utils.PostViewType
import com.ducktapedapps.updoot.utils.PostViewType.COMPACT
import com.ducktapedapps.updoot.utils.PostViewType.LARGE

@Composable
fun SubredditScreen(
    subredditName: String,
    publishEvent: (Event) -> Unit
) {
    val viewModel: SubredditVM = hiltViewModel<SubredditVMImpl>().apply {
        setSubredditName(subredditName)
    }
    val viewState = viewModel.viewState.collectAsState()
    Scaffold(
        topBar = { SubredditTopBar(subredditName = viewState.value.subredditName) }
    ) {
        SubredditFeed(
            feed = viewState.value.feed,
            postViewType = viewState.value.subredditPrefs.viewType,
            loadPage = viewModel::loadPage,
            publishEvent = publishEvent
        )
    }
}


@Composable
private fun SubredditTopBar(subredditName: String) {
    Surface(elevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center
        ) { Text(text = subredditName) }
    }
}

@Composable
private fun SubredditFeed(
    feed: PagingModel<List<PostUiModel>>,
    postViewType: PostViewType,
    loadPage: () -> Unit,
    publishEvent: (Event) -> Unit,
) {
    LazyColumn {
        itemsIndexed(feed.content) { index, post ->
            LaunchedEffect(key1 = Unit) {
                with(feed) {
                    if (index >= content.size - 10 && footer is UnLoadedPage) loadPage()
                }
            }
            when (postViewType) {
                COMPACT -> CompactPost(post = post, publishEvent = publishEvent)
                LARGE -> LargePost(post = post, publishEvent = publishEvent)
            }
        }
        item {
            when (val footer = feed.footer) {
                End -> PageEnd()
                is Error -> PageLoadingFailed(
                    performRetry = loadPage,
                    message = footer.exception.message
                        ?: stringResource(id = R.string.something_went_wrong)
                )
                Loading -> PageLoading()
                is UnLoadedPage -> Unit
            }
        }
        item {
            Spacer(modifier = Modifier.padding(64.dp))
        }
    }
}
