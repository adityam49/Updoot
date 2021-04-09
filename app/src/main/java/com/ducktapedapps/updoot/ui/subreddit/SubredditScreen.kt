package com.ducktapedapps.updoot.ui.subreddit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.ui.ActivityVM
import com.ducktapedapps.updoot.ui.common.PageEnd
import com.ducktapedapps.updoot.ui.common.PageLoading
import com.ducktapedapps.updoot.ui.common.PageLoadingFailed
import com.ducktapedapps.updoot.ui.common.SubredditBottomBar
import com.ducktapedapps.updoot.ui.navDrawer.NavigationMenuScreen
import com.ducktapedapps.updoot.ui.subreddit.ActiveContent.SubredditInfo
import com.ducktapedapps.updoot.ui.theme.BottomDrawerColor
import com.ducktapedapps.updoot.ui.theme.UpdootDarkColors
import com.ducktapedapps.updoot.utils.Page.*
import com.ducktapedapps.updoot.utils.PostViewType.COMPACT
import com.ducktapedapps.updoot.utils.PostViewType.LARGE
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

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
    val activeContent = remember { mutableStateOf<ActiveContent>(SubredditInfo) }
    if (bottomSheetState.isCollapsed) activeContent.value = SubredditInfo

    BottomSheetScaffold(
        scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState),
        sheetBackgroundColor = MaterialTheme.colors.BottomDrawerColor,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetPeekHeight = 64.dp,
        sheetElevation = 1.dp,
        sheetContent = {
            Surface(
                color = MaterialTheme.colors.BottomDrawerColor,
                contentColor = UpdootDarkColors.onSurface
            ) {
                Column {
                    SubredditBottomBar(
                        modifier = Modifier.padding(4.dp),
                        navigateUp = { },
                        openMenu = {
                            activeContent.value = ActiveContent.Menu
                            coroutineScope.launch { bottomSheetState.expand() }
                        },
                        subredditName = if (viewModel.subredditName.isBlank()) stringResource(R.string.app_name) else viewModel.subredditName,
                        showActionIcons = !bottomSheetState.isExpanded
                    )
                    when (activeContent.value) {
                        ActiveContent.Menu -> NavigationMenuScreen(
                            viewModel = activityVM,
                            openSubreddit = openSubreddit,
                            openUser = openUser
                        )
                        SubredditInfo -> SubredditInfo(subredditVM = viewModel)
                    }
                }
            }
        },
        content = { Body(viewModel, openMedia, openComments, openSubreddit, openUser) }
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

sealed class ActiveContent {
    object SubredditInfo : ActiveContent()
    object Menu : ActiveContent()
}