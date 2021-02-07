package com.ducktapedapps.updoot.ui.subreddit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.unit.dp
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.ui.ActivityVM
import com.ducktapedapps.updoot.ui.common.BottomBarActions
import com.ducktapedapps.updoot.ui.common.FancyBottomBar
import com.ducktapedapps.updoot.ui.navDrawer.NavigationMenuScreen
import com.ducktapedapps.updoot.ui.subreddit.ActiveContent.*
import com.ducktapedapps.updoot.ui.theme.BottomDrawerColor
import com.ducktapedapps.updoot.ui.theme.UpdootDarkColors
import com.ducktapedapps.updoot.utils.Media
import com.ducktapedapps.updoot.utils.SubmissionUiType.COMPACT
import com.ducktapedapps.updoot.utils.SubmissionUiType.LARGE
import com.ducktapedapps.updoot.utils.toMedia
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun SubredditScreen(
        viewModel: SubmissionsVM,
        openMedia: (Media) -> Unit,
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
            sheetShape = RoundedCornerShape(topLeft = 16.dp, topRight = 16.dp),
            sheetPeekHeight = 64.dp,
            sheetElevation = 1.dp,
            sheetContent = {
                FancyBottomBar(
                        modifier = Modifier.padding(top = 8.dp),
                        sheetProgress = bottomSheetState.progress,
                        options = subredditBottomBarActions,
                        title = when (activeContent.value) {
                            SubredditInfo -> {
                                if (viewModel.subreddit.isEmpty())
                                    AmbientContext.current.getString(R.string.app_name)
                                else viewModel.subreddit + "/info"
                            }
                            GlobalMenu -> AmbientContext.current.getString(R.string.app_name)
                            Search -> "Search"
                            null -> {
                                if (viewModel.subreddit.isBlank())
                                    AmbientContext.current.getString(R.string.app_name)
                                else
                                    viewModel.subreddit
                            }
                        },
                        navigateUp = {},
                )
                Surface(color = MaterialTheme.colors.BottomDrawerColor, contentColor = UpdootDarkColors.onSurface) {
                    when (activeContent.value) {
                        GlobalMenu -> NavigationMenuScreen(viewModel = activityVM)
                        SubredditInfo -> SubredditInfo(submissionsVM = viewModel)
                        else -> EmptyScreen()
                    }
                }
            },
            bodyContent = { Body(viewModel, openMedia, openComments, openSubreddit, openUser) }
    )
}

@Composable
fun Body(
        viewModel: SubmissionsVM,
        openMedia: (Media) -> Unit,
        openComments: (subreddit: String, id: String) -> Unit,
        openSubreddit: (String) -> Unit,
        openUser: (String) -> Unit,
) {
    val feed = viewModel.feedPages.collectAsState()
    val postType = viewModel.postViewType.collectAsState()
    val loading = viewModel.isLoading.collectAsState()
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
        LazyColumn {
            itemsIndexed(items = feed.value) { index, item ->
                LaunchedEffect(key1 = "onActive?" /* TODO : Lookup source docs */) {
                    viewModel.lastScrollPosition = index
                    //TODO move paging stuff to viewModel
                    if (index >= feed.value.size - 10 && viewModel.hasNextPage() && !viewModel.isLoading.value) viewModel.loadPage()
                }
                when (postType.value) {
                    COMPACT -> CompactPost(
                            linkData = item,
                            onClickMedia = { openMedia(item.toMedia()) },
                            onClickPost = { openComments(item.subredditName, item.name) },
                            openSubreddit = openSubreddit,
                            openUser = openUser,
                    )
                    LARGE -> LargePost(
                            linkData = item,
                            onClickMedia = { openMedia(item.toMedia()) },
                            openPost = { openComments(item.subredditName, item.name) },
                            openSubreddit = openSubreddit,
                            openUser = openUser,
                    )
                }
                Divider()
            }
            if (loading.value) item {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillParentMaxWidth()) {
                    CircularProgressIndicator(modifier = Modifier
                            .size(64.dp)
                            .padding(16.dp))
                }
            }
            item { Spacer(Modifier.height(200.dp)) }
        }
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