package com.ducktapedapps.updoot.ui.subreddit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.ui.ActivityVM
import com.ducktapedapps.updoot.ui.common.*
import com.ducktapedapps.updoot.ui.navDrawer.NavigationMenuScreen
import com.ducktapedapps.updoot.ui.subreddit.ActiveContent.*
import com.ducktapedapps.updoot.ui.theme.BottomDrawerColor
import com.ducktapedapps.updoot.ui.theme.UpdootDarkColors
import com.ducktapedapps.updoot.utils.Page.*
import com.ducktapedapps.updoot.utils.PostViewType.COMPACT
import com.ducktapedapps.updoot.utils.PostViewType.LARGE
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun SubredditScreen(
    viewModel: SubredditVM,
    openMedia: (PostMedia) -> Unit,
    openComments: (subreddit: String, id: String) -> Unit,
    openUser: (String) -> Unit,
    openSubreddit: (String) -> Unit,
    activityVM: ActivityVM,
) {
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(key1 = Unit) {
        activityVM
            .shouldReload
            .onEach { viewModel.reload() }
            .launchIn(coroutineScope)

    }
    val bottomSheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    val activeContent = remember { mutableStateOf<ActiveContent?>(null) }
    if (bottomSheetState.isCollapsed) activeContent.value = null
    val subredditBottomBarActions = listOf(
        BottomBarActions(Icons.Default.Info, "Info") {
            activeContent.value = SubredditInfo
            bottomSheetState.expand()
        },
        BottomBarActions(Icons.Default.Search, "Search") {
            activeContent.value = Search
            bottomSheetState.expand()
        },
        BottomBarActions(Icons.Default.Menu, "GlobalMenu") {
            activeContent.value = GlobalMenu
            bottomSheetState.expand()
        }
    )

    BottomSheetScaffold(
        sheetGesturesEnabled = bottomSheetState.isExpanded,
        scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState),
        sheetBackgroundColor = MaterialTheme.colors.BottomDrawerColor,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetPeekHeight = 64.dp,
        sheetElevation = 1.dp,
        sheetContent = {
            FancyBottomBar(
                modifier = Modifier.padding(top = 8.dp),
                sheetProgress = bottomSheetState.progress,
                options = subredditBottomBarActions,
                title = when (activeContent.value) {
                    SubredditInfo -> {
                        if (viewModel.subredditName.isEmpty()) stringResource(R.string.app_name)
                        else viewModel.subredditName + "/info"
                    }
                    GlobalMenu -> stringResource(R.string.app_name)
                    Search -> "Search"
                    null -> {
                        if (viewModel.subredditName.isBlank())
                            stringResource(R.string.app_name)
                        else
                            viewModel.subredditName
                    }
                },
                navigateUp = {},
            )
            Surface(
                color = MaterialTheme.colors.BottomDrawerColor,
                contentColor = UpdootDarkColors.onSurface
            ) {
                when (activeContent.value) {
                    GlobalMenu -> NavigationMenuScreen(
                        viewModel = activityVM,
                        openSubreddit = openSubreddit
                    )
                    SubredditInfo -> SubredditInfo(subredditVM = viewModel)
                    else -> EmptyScreen()
                }
            }
        },
        bodyContent = { Body(viewModel, openMedia, openComments, openSubreddit, openUser) }
    )
}

@Composable
fun Body(
    viewModel: SubredditVM,
    openMedia: (PostMedia) -> Unit,
    openComments: (subreddit: String, id: String) -> Unit,
    openSubreddit: (String) -> Unit,
    openUser: (String) -> Unit,
) {
    val feed = viewModel.pagesOfPosts.collectAsState()
    val postType = viewModel.postViewType.collectAsState()

    LazyColumn {
        items(feed.value) { currentPage ->
            when (currentPage) {
                LoadingPage -> PageLoading()

                is LoadedPage -> {
                    LaunchedEffect(Unit) { if (currentPage.hasNextPage()) viewModel.loadPage() }
                    currentPage.content.forEach { post ->
                        when (postType.value) {
                            COMPACT -> CompactPost(
                                post = post,
                                onClickMedia = { openMedia(post.postMedia) },
                                onClickPost = { openComments(post.subredditName, post.id) },
                                openSubreddit = openSubreddit,
                                openUser = openUser,
                            )

                            LARGE -> LargePost(
                                post = post,
                                onClickMedia = { openMedia(post.postMedia) },
                                openPost = { openComments(post.subredditName, post.id) },
                                openSubreddit = openSubreddit,
                                openUser = openUser,
                            )
                        }
                    }
                }
                is ErrorPage -> PageLoadingFailed(
                    performRetry = viewModel::loadPage,
                    message = currentPage.errorReason
                )
                End -> PageEnd()
            }
            Divider()
        }
        item { Spacer(Modifier.height(500.dp)) }
    }
}


@Composable
fun EmptyScreen() {
    Box(modifier = Modifier.fillMaxSize()) { }
}

sealed class ActiveContent {
    object SubredditInfo : ActiveContent()
    object Search : ActiveContent()
    object GlobalMenu : ActiveContent()
}