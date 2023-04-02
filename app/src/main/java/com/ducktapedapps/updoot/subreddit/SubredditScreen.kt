@file:OptIn(ExperimentalMaterial3Api::class)

package com.ducktapedapps.updoot.subreddit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ducktapedapps.navigation.Event
import com.ducktapedapps.navigation.Event.ScreenNavigationEvent
import com.ducktapedapps.navigation.NavigationDirections.SubredditOptionsNavigation
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
    publishEvent: (Event) -> Unit,
) {
    val viewModel: SubredditVM = hiltViewModel<SubredditVMImpl>().apply {
        setSubredditName(subredditName)
    }
    val viewState = viewModel.viewState.collectAsState()
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SubredditTopBar(
                modifier = Modifier.fillMaxWidth(),
                subredditName = viewState.value.subredditName,
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier,
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = {
                    publishEvent(
                        ScreenNavigationEvent(
                            SubredditOptionsNavigation.open(subredditName)
                        )
                    )
                }) { Icon(Icons.Outlined.Menu, Icons.Outlined.Menu.name) }
        }
    ) { scaffoldPaddingValues ->
        SubredditFeed(
            modifier = Modifier.padding(scaffoldPaddingValues),
            feed = viewState.value.feed,
            postViewType = viewState.value.subredditPrefs.viewType,
            loadPage = viewModel::loadPage,
            publishEvent = publishEvent,
            listState = listState,

            )
    }
}

@Composable
private fun SubredditTopBar(
    modifier: Modifier = Modifier,
    subredditName: String,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = subredditName,
                style = MaterialTheme.typography.titleLarge,
                overflow = TextOverflow.Ellipsis,
            )
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun SubredditFeed(
    modifier: Modifier = Modifier,
    feed: PagingModel<List<PostUiModel>>,
    postViewType: PostViewType,
    loadPage: () -> Unit,
    publishEvent: (Event) -> Unit,
    listState: LazyListState,
) {
    LazyColumn(
        modifier = modifier,
        state = listState,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        itemsIndexed(
            items = feed.content,
            key = { _, item -> item.id }
        ) { index, post ->
            LaunchedEffect(key1 = Unit) {
                with(feed) {
                    if (index >= content.size - 10 && footer is UnLoadedPage) loadPage()
                }
            }
            when (postViewType) {
                COMPACT -> CompactPost(
                    post = post,
                    publishEvent = publishEvent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                )
                LARGE -> LargePost(
                    post = post,
                    publishEvent = publishEvent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                )
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
