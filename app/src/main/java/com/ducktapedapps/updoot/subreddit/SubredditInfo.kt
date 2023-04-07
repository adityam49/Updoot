package com.ducktapedapps.updoot.subreddit

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ducktapedapps.navigation.Event
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.common.PageLoading
import com.ducktapedapps.updoot.common.PageLoadingFailed
import com.ducktapedapps.updoot.subreddit.SubredditInfoState.*
import com.ducktapedapps.updoot.theme.BottomDrawerColor
import com.ducktapedapps.updoot.theme.SurfaceOnDrawer
import com.ducktapedapps.updoot.utils.PostViewType
import com.ducktapedapps.updoot.utils.PostViewType.COMPACT
import com.ducktapedapps.updoot.utils.PostViewType.LARGE
import com.ducktapedapps.updoot.utils.getCompactAge
import com.ducktapedapps.updoot.utils.getCompactCountAsString
import java.util.*


/**
 *  Subreddit sidebar UI component
 */
@Composable
fun SubredditInfo(subredditName: String, publishEvent: (Event) -> Unit) {
    val viewModel: SubredditVM = hiltViewModel<SubredditVMImpl>()
    val viewState = viewModel.viewState.collectAsState()

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (header, info, viewType) = createRefs()

        when (val data = viewState.value.subredditInfo) {
            Loading -> PageLoading()
            is UiModel -> {
                SubredditInfoHeader(
                    iconUrl = data.icon,
                    activeMembers = data.activeAccounts,
                    subscribers = data.subscribers,
                    created = data.created,
                    modifier = Modifier
                        .fillMaxWidth()
                        .constrainAs(header) {
                            top.linkTo(parent.top)
                            bottom.linkTo(viewType.top)
                            height = Dimension.wrapContent
                        },
                    isSubscribed = viewState.value.subscriptionState,
                    toggleSubscription = viewModel::toggleSubredditSubscription,
                )
                SubmissionViewType(
                    type = viewState.value.subredditPrefs.viewType,
                    setType = viewModel::setPostViewType,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .constrainAs(viewType) {
                            top.linkTo(header.bottom)
                            bottom.linkTo(info.top)
                            height = Dimension.wrapContent
                        }
                )
                Info(
                    description = data.info,
                    modifier = Modifier
                        .fillMaxWidth()
                        .constrainAs(info) {
                            top.linkTo(viewType.bottom)
                        }
                )
            }
            is LoadingFailed -> PageLoadingFailed(
                performRetry = viewModel::loadSubredditInfo,
                message = data.reason
            )
            null -> TODO()
        }
    }
}

@Composable
private fun SubredditInfoHeader(
    modifier: Modifier,
    iconUrl: String?,
    activeMembers: Long?,
    subscribers: Long?,
    created: Date,
    isSubscribed: Boolean?,
    toggleSubscription: () -> Unit,
) {
    DrawerCard(modifier = modifier) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = iconUrl,
                error = painterResource(id = R.drawable.ic_subreddit_default_24dp),
                contentDescription = stringResource(id = R.string.subreddit_icon),
                modifier = Modifier
                    .size(48.dp)
                    .clip(shape = CircleShape),
            )
            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = (subscribers?.run { getCompactCountAsString(this) + " Subscribers " }
                    ?: "") +
                        (activeMembers?.run { " • " + getCompactCountAsString(this) + " active " }
                            ?: "") +
                        created.run { "\n" + getCompactAge(time) },
                style = MaterialTheme.typography.labelSmall
            )
            Button(
                onClick = { if (isSubscribed != null) toggleSubscription() },
                enabled = isSubscribed != null
            ) {
                Text(
                    text = if (isSubscribed == true) stringResource(R.string.leave)
                    else stringResource(R.string.join)
                )
            }
        }
    }
}

@Composable
private fun SubmissionViewType(
    modifier: Modifier,
    type: PostViewType,
    setType: (PostViewType) -> Unit
) {
    DrawerCard(modifier = modifier) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = "View Type", style = MaterialTheme.typography.labelMedium)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                SelectableViewType(
                    postViewType = COMPACT,
                    isSelected = type == COMPACT,
                    selectViewType = { setType(COMPACT) })
                SelectableViewType(
                    postViewType = LARGE,
                    isSelected = type == LARGE,
                    selectViewType = { setType(LARGE) })
            }
            Spacer(modifier = Modifier)
        }
    }
}

@Composable
fun SelectableViewType(
    postViewType: PostViewType,
    isSelected: Boolean,
    selectViewType: () -> Unit
) {
    Box(
        modifier = Modifier
            .wrapContentSize()
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.BottomDrawerColor.copy(alpha = if (isSelected) 1f else 0.1f),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { selectViewType() }
            .padding(8.dp)
    ) {
        Icon(
            painter = painterResource(
                id = when (postViewType) {
                    COMPACT -> R.drawable.ic_list_view_24dp
                    LARGE -> R.drawable.ic_card_view_24dp
                }
            ), "ViewType Icon"
        )
    }
}

@Composable
fun DrawerCard(
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.SurfaceOnDrawer,
        content = content
    )
}

@Composable
private fun Info(modifier: Modifier, description: String?) {
    DrawerCard(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize()
        ) {
            item {
                Text(
                    text = description ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            item { Spacer(modifier = Modifier.padding(100.dp)) }
        }
    }
}