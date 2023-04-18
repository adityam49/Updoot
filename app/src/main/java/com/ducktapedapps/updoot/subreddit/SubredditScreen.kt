@file:OptIn(ExperimentalMaterial3Api::class)

package com.ducktapedapps.updoot.subreddit

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ducktapedapps.navigation.Event
import com.ducktapedapps.navigation.Event.*
import com.ducktapedapps.navigation.NavigationDirections.*
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.common.BottomSheetItemType.*
import com.ducktapedapps.updoot.common.ModalBottomSheetForActions
import com.ducktapedapps.updoot.common.PageEnd
import com.ducktapedapps.updoot.common.PageLoading
import com.ducktapedapps.updoot.common.PageLoadingFailed
import com.ducktapedapps.updoot.subreddit.ScreenAction.*
import com.ducktapedapps.updoot.utils.PagingModel
import com.ducktapedapps.updoot.utils.PagingModel.Footer.*
import com.ducktapedapps.updoot.utils.PostViewType
import com.ducktapedapps.updoot.utils.PostViewType.COMPACT
import com.ducktapedapps.updoot.utils.PostViewType.LARGE

@Composable
fun SubredditScreen(
    publishEvent: (Event) -> Unit,
) {
    val viewModel: SubredditVM = hiltViewModel<SubredditVMImpl>()
    val viewState = viewModel.viewState.collectAsStateWithLifecycle().value

    var bottomSheetVisible by rememberSaveable { mutableStateOf(false) }
    ModalBottomSheetForActions(
        bottomSheetVisible = bottomSheetVisible,
        publishEvent = {
            bottomSheetVisible = false
            publishEvent(it)
        },
        options = viewState.screenBottomSheetOptions,
        hideBottomSheet = { bottomSheetVisible = false }
    )
    Scaffold(
        modifier = Modifier,
        topBar = {
            SubredditTopBar(
                modifier = Modifier.fillMaxWidth(),
                subredditName = viewState.subredditPrefs.subredditName,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier,
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = {
                    viewModel.doAction(ShowSubredditOptions)
                    bottomSheetVisible = true
                }) { Icon(Icons.Outlined.Menu, Icons.Outlined.Menu.name) }
        }
    ) { scaffoldPaddingValues ->
        SubredditFeed(
            modifier = Modifier.padding(scaffoldPaddingValues),
            feed = viewState.feed,
            postViewType = viewState.subredditPrefs.viewType,
            doAction = {
                when (it) {
                    is ShowPostOptions, ShowSubredditOptions -> bottomSheetVisible = true
                    else -> Unit
                }
                viewModel.doAction(it)
            },
            isLoggedIn = viewState.isLoggedIn,
            publishEvent = publishEvent,
        )
    }

}


@Composable
private fun SubredditTopBar(
    modifier: Modifier = Modifier,
    subredditName: String,
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
    )
}

@Composable
private fun SubredditFeed(
    modifier: Modifier = Modifier,
    feed: PagingModel<List<PostUiModel>>,
    postViewType: PostViewType,
    doAction: (ScreenAction) -> Unit,
    publishEvent: (Event) -> Unit,
    isLoggedIn: Boolean,
) {
    val listState = rememberLazyListState()
    LazyColumn(
        modifier = modifier,
        state = listState,
    ) {
        itemsIndexed(
            items = feed.content,
            key = { _, item -> item.id }
        ) { index, post ->
            LaunchedEffect(key1 = Unit) {
                with(feed) {
                    if (index >= content.size - 10 && footer is UnLoadedPage) doAction(
                        LoadPage
                    )
                }
            }
            when (postViewType) {
                COMPACT -> CompactPost(
                    post = post,
                    publishEvent = publishEvent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .combinedClickable(
                            onClick = {
                                publishEvent(
                                    ScreenNavigationEvent(
                                        CommentScreenNavigation.open(
                                            post.subredditName,
                                            post.id
                                        )
                                    )
                                )
                            },
                            onLongClick = { doAction(ShowPostOptions(post)) }
                        )
                        .padding(vertical = 8.dp),
                    isLoggedIn = isLoggedIn,
                    doAction = doAction,
                )

                LARGE -> {
                    LargePost(
                        post = post,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .combinedClickable(
                                onClick = {
                                    publishEvent(
                                        ScreenNavigationEvent(
                                            CommentScreenNavigation.open(
                                                post.subredditName,
                                                post.id
                                            )
                                        )
                                    )
                                },
                                onLongClick = { doAction(ShowPostOptions(post)) }
                            )
                            .padding(vertical = 8.dp),
                        isLoggedIn = isLoggedIn,
                        doAction = doAction,
                    )
                    if (index != feed.content.lastIndex)
                        Divider(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )
                }
            }

        }
        item {
            when (val footer = feed.footer) {
                End -> PageEnd()
                is Error -> PageLoadingFailed(
                    performRetry = { doAction(LoadPage) },
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
