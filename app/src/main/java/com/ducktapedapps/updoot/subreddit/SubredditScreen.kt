package com.ducktapedapps.updoot.subreddit

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.hilt.navigation.compose.hiltViewModel
import com.ducktapedapps.navigation.Event
import com.ducktapedapps.navigation.Event.ScreenNavigationEvent
import com.ducktapedapps.navigation.NavigationDirections.SubredditOptionsNavigation
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.common.PageEnd
import com.ducktapedapps.updoot.common.PageLoading
import com.ducktapedapps.updoot.common.PageLoadingFailed
import com.ducktapedapps.updoot.puck.Utils
import com.ducktapedapps.updoot.puck.puck
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
    val parentSize = remember { mutableStateOf(Size.Zero) }
    val listState = rememberLazyListState()
    Scaffold(
            modifier = Modifier.padding(bottom = 50.dp),
            topBar = {
                SubredditTopBar(
                        modifier = Modifier.fillMaxWidth(),
                        subredditName = viewState.value.subredditName
                )
            }) { scaffoldPaddingValues ->

        Box(
                Modifier
                        .onGloballyPositioned {
                            it.parentCoordinates?.size
                            parentSize.value = it.size.toSize()
                        }
                        .padding(scaffoldPaddingValues)
        ) {
            SubredditFeed(
                    modifier = Modifier,
                    feed = viewState.value.feed,
                    postViewType = viewState.value.subredditPrefs.viewType,
                    loadPage = viewModel::loadPage,
                    publishEvent = publishEvent,
                    listState = listState,

                    )
            DraggableFab(
                    parentSize = parentSize,
                    publishEvent = publishEvent,
                    subredditName = subredditName,
                    scrollBy = {
                        listState.animateScrollBy(it)
                    }
            )

        }
    }
}

@Composable
private fun DraggableFab(
        modifier: Modifier = Modifier,
        parentSize: MutableState<Size>,
        publishEvent: (Event) -> Unit,
        subredditName: String,
        scrollBy: suspend (Float) -> Unit,
) {
    Box(
            modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned {
                        parentSize.value = Size(it.size.width.toFloat(), it.size.height.toFloat())
                    }
    ) {
        //TODO : map scroll delta to animated icon of fab (down chevron and up chevron)
        val previousPointState = remember { mutableStateOf(Offset.Zero) }
        val latestPointState = remember { mutableStateOf(Offset.Zero) }
        LaunchedEffect(previousPointState.value, latestPointState.value) {
            //TODO : fix this
            scrollBy(latestPointState.value.y - previousPointState.value.y)
        }
        FloatingActionButton(
                modifier = Modifier.puck(
                        parentSize = parentSize,
                        behaviour = Utils.Behaviour.FreeForm,
                        previousPointState = previousPointState,
                        latestPointState = latestPointState,
                ),
                backgroundColor = MaterialTheme.colors.primary,
                onClick = {
                    publishEvent(
                            ScreenNavigationEvent(
                                    SubredditOptionsNavigation.open(subredditName)
                            )
                    )
                }) {
            Icon(Icons.Outlined.Menu, Icons.Outlined.Menu.name)
        }
    }

}

@Composable
private fun SubredditTopBar(
        modifier: Modifier = Modifier,
        subredditName: String
) {
    Surface(modifier = modifier, elevation = 8.dp) {
        Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.Center
        ) { Text(text = subredditName) }
    }
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
    LazyColumn(state = listState) {
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
