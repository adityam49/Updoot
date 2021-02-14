package com.ducktapedapps.updoot.ui.subreddit

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.transform.CircleCropTransformation
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.ui.common.PageLoading
import com.ducktapedapps.updoot.ui.common.PageLoadingFailed
import com.ducktapedapps.updoot.ui.subreddit.SubredditInfoState.*
import com.ducktapedapps.updoot.ui.theme.BottomDrawerColor
import com.ducktapedapps.updoot.ui.theme.SurfaceOnDrawer
import com.ducktapedapps.updoot.utils.PostViewType
import com.ducktapedapps.updoot.utils.PostViewType.COMPACT
import com.ducktapedapps.updoot.utils.PostViewType.LARGE
import com.ducktapedapps.updoot.utils.getCompactAge
import com.ducktapedapps.updoot.utils.getCompactCountAsString
import dev.chrisbanes.accompanist.coil.CoilImage
import dev.chrisbanes.accompanist.imageloading.ImageLoadState
import java.util.*


/**
 *  Subreddit sidebar UI component
 */
@Composable
fun SubredditInfo(subredditVM: SubredditVM) {
    val subredditInfo = subredditVM.subredditInfo.collectAsState()
    val postType = subredditVM.postViewType.collectAsState()

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (header, info, footer) = createRefs()

        when (val data = subredditInfo.value) {
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
                            bottom.linkTo(info.top)
                            height = Dimension.wrapContent
                        }
                )
                Info(
                    description = data.info,
                    modifier = Modifier
                        .fillMaxWidth()
                        .constrainAs(info) {
                            top.linkTo(header.bottom)
                            bottom.linkTo(footer.top)
                            height = Dimension.fillToConstraints
                        }
                )
            }
            is LoadingFailed -> PageLoadingFailed(
                performRetry = subredditVM::loadSubredditInfo,
                message = data.reason
            )
        }
        SubmissionViewType(
            type = postType.value,
            setType = subredditVM::setPostViewType,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .constrainAs(footer) {
                    bottom.linkTo(parent.bottom)
                    height = Dimension.fillToConstraints
                }
        )
    }
}

@Composable
private fun SubredditInfoHeader(
    modifier: Modifier,
    iconUrl: String?,
    activeMembers: Long?,
    subscribers: Long?,
    created: Date,
) {
    DrawerCard(modifier = modifier) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CoilImage(
                data = iconUrl ?: "",
                requestBuilder = { transformations(CircleCropTransformation()) },
                modifier = Modifier.size(48.dp),
            ) { imageLoadState ->
                when (imageLoadState) {
                    is ImageLoadState.Success -> Image(
                        painter = imageLoadState.painter,
                        contentDescription = "Subreddit Icon"
                    )
                    else -> Icon(
                        painter = painterResource(id = R.drawable.ic_subreddit_default_24dp),
                        "Subreddit Icon"
                    )
                }
            }

            Providers(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    modifier = Modifier.padding(start = 16.dp),
                    text = (subscribers?.run { getCompactCountAsString(this) + " Subscribers " }
                        ?: "") +
                            (activeMembers?.run { " • " + getCompactCountAsString(this) + " active " }
                                ?: "") +
                            created.run { "\n" + getCompactAge(time) },
                    style = MaterialTheme.typography.caption
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
            Text(text = "View Type", style = MaterialTheme.typography.caption)
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
                color = contentColorFor(MaterialTheme.colors.BottomDrawerColor.copy(alpha = if (isSelected) 1f else 0.1f)),
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
        color = MaterialTheme.colors.SurfaceOnDrawer,
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
            item { Text(text = description ?: "", style = MaterialTheme.typography.body1) }
        }
    }
}