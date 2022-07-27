package com.ducktapedapps.updoot.subreddit

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.ducktapedapps.navigation.Event
import com.ducktapedapps.navigation.Event.*
import com.ducktapedapps.navigation.NavigationDirections.*
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.common.*
import com.ducktapedapps.updoot.theme.StickyPostColor
import com.ducktapedapps.updoot.utils.getCompactCountAsString
import com.ducktapedapps.updoot.utils.getCompactDateAsString
import kotlin.math.absoluteValue


@Composable
fun CompactPost(
    post: PostUiModel,
    publishEvent: (Event) -> Unit
) {
    val (optionsDialog, setOptionsDialogVisibility) = remember { mutableStateOf(false) }

    val list = listOf(
        MenuItemModel({
            setOptionsDialogVisibility(false)
            publishEvent(post.openSubreddit())
        }, post.subredditName, R.drawable.ic_subreddit_default_24dp),
        MenuItemModel({
            setOptionsDialogVisibility(false)
            publishEvent(post.openAuthorScreen())
        }, post.author, R.drawable.ic_account_circle_24dp),
    )

    if (optionsDialog) OptionsDialog(
        options = list,
        dismiss = { setOptionsDialogVisibility(false) })
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .combinedClickable(
                onClick = { publishEvent(post.openComments()) },
                onLongClick = { setOptionsDialogVisibility(true) })
    ) {
        val (mediaThumbnail, title, metaData, voteCounter, gildings) = createRefs()
        CompactMediaThumbnail(
            post = post,
            modifier = Modifier
                .clip(CircleShape)
                .requiredSize(48.dp)
                .clickable(onClick = { publishEvent(post.openPostMedia()) })
                .constrainAs(mediaThumbnail) {
                    start.linkTo(parent.start, margin = 8.dp)
                    top.linkTo(parent.top, margin = 8.dp)
                    end.linkTo(title.start)
                })
        SubmissionTitle(
            title = post.title,
            isSticky = post.isSticky,
            modifier = Modifier.constrainAs(title) {
                start.linkTo(mediaThumbnail.end, 8.dp)
                top.linkTo(parent.top, margin = 8.dp)
                end.linkTo(voteCounter.start, margin = 8.dp)
                bottom.linkTo(metaData.top, margin = 8.dp)
                height = Dimension.wrapContent
                width = Dimension.fillToConstraints
            })

        MetaData(post = post, modifier = Modifier.constrainAs(metaData) {
            start.linkTo(title.start)
            top.linkTo(title.bottom, margin = 8.dp)
            bottom.linkTo(parent.bottom, margin = 8.dp)
            end.linkTo(gildings.start, margin = 8.dp)
            width = Dimension.fillToConstraints
            height = Dimension.wrapContent
        })
        VoteCounter(
            upVotes = post.upVotes,
            userHasUpVoted = post.userHasUpVoted,
            modifier = Modifier.constrainAs(voteCounter) {
                top.linkTo(parent.top, margin = 8.dp)
                end.linkTo(parent.end, margin = 8.dp)
                start.linkTo(title.end)
                width = Dimension.wrapContent
            }
        )
        AllGildings(gildings = post.gildings, modifier = Modifier.constrainAs(gildings) {
            end.linkTo(parent.end, margin = 8.dp)
            top.linkTo(metaData.top)
            bottom.linkTo(metaData.bottom)
        })
    }
}

@Composable
fun CompactMediaThumbnail(post: PostUiModel, modifier: Modifier) {
    if (post.isNsfw) Image(
        modifier = modifier,
        painter = painterResource(id = R.drawable.ic_nsfw_24dp),
        contentDescription = "NSFW Content"
    )
    else {
        when (post.thumbnail) {
            is Thumbnail.Remote ->
                Image(
                    painter = rememberImagePainter(data = post.thumbnail.url) {
                        error(post.thumbnail.fallbackLocalThumbnail)
                        transformations(CircleCropTransformation())
                    },
                    contentDescription = stringResource(R.string.post_thumbnail),
                    modifier = modifier,
                )
            is Thumbnail.LocalThumbnail -> Image(
                painter = painterResource(id = post.thumbnail.imageResource),
                contentDescription = "Error Icon",
                modifier = modifier
            )
        }
    }
}


@Composable
fun SubmissionTitle(
    title: String,
    isSticky: Boolean,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
        Text(
            text = title,
            color = if (isSticky) MaterialTheme.colors.StickyPostColor else MaterialTheme.colors.onBackground,
            style = MaterialTheme.typography.h5,
            modifier = modifier
        )
    }
}

@Composable
private fun MetaData(post: PostUiModel, modifier: Modifier) {
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled) {
        Text(
            style = MaterialTheme.typography.caption,
            text = "${post.subredditName} • ${getCompactCountAsString(post.replyCount.toLong())} Replies • ${
                getCompactDateAsString(
                    post.creationDate.time
                )
            }",
            modifier = modifier
        )
    }
}

@Composable
fun LargePost(
    post: PostUiModel,
    publishEvent: (Event) -> Unit,
) {
    val (optionsDialog, setOptionsDialogVisibility) = remember { mutableStateOf(false) }

    val list = listOf(
        MenuItemModel({
            setOptionsDialogVisibility(false)
            publishEvent(post.openSubreddit())
        }, post.subredditName, R.drawable.ic_subreddit_default_24dp),
        MenuItemModel({
            setOptionsDialogVisibility(false)
            publishEvent(post.openAuthorScreen())
        }, post.author, R.drawable.ic_account_circle_24dp),
    )
    if (optionsDialog) OptionsDialog(
        dismiss = { setOptionsDialogVisibility(false) },
        options = list
    )
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .combinedClickable(
                onClick = { publishEvent(post.openComments()) },
                onLongClick = { setOptionsDialogVisibility(true) })
    ) {
        val (media, title, metaData, voteCounter, gildings) = createRefs()
        SubmissionTitle(
            title = post.title,
            isSticky = post.isSticky,
            modifier = Modifier.constrainAs(title) {
                start.linkTo(parent.start, margin = 8.dp)
                top.linkTo(parent.top, margin = 8.dp)
                end.linkTo(voteCounter.start, margin = 8.dp)
                bottom.linkTo(media.top, margin = 8.dp)
                height = Dimension.wrapContent
                width = Dimension.fillToConstraints
            })

        LargePostMedia(
            postMedia = post.postMedia,
            modifier = Modifier
                .constrainAs(media) {
                    start.linkTo(parent.start)
                    top.linkTo(title.bottom)
                    end.linkTo(parent.end)
                    bottom.linkTo(metaData.top)
                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                }
                .combinedClickable(
                    onClick = { publishEvent(post.openPostMedia()) },
                    onLongClick = { setOptionsDialogVisibility(true) }
                )
        )

        MetaData(post = post, modifier = Modifier.constrainAs(metaData) {
            start.linkTo(media.start, margin = 8.dp)
            top.linkTo(media.bottom, margin = 8.dp)
            bottom.linkTo(parent.bottom, margin = 16.dp)
            width = Dimension.fillToConstraints
            height = Dimension.wrapContent
        })
        VoteCounter(
            upVotes = post.upVotes,
            userHasUpVoted = post.userHasUpVoted,
            modifier = Modifier.constrainAs(voteCounter) {
                top.linkTo(title.top)
                end.linkTo(parent.end, margin = 8.dp)
                start.linkTo(title.end)
                width = Dimension.wrapContent
            }
        )
        AllGildings(gildings = post.gildings, modifier = Modifier.constrainAs(gildings) {
            end.linkTo(parent.end, margin = 8.dp)
            top.linkTo(metaData.top)
            bottom.linkTo(metaData.bottom)
        })
    }
}

@Composable
fun LargePostMedia(postMedia: PostMedia, modifier: Modifier) {
    when (postMedia) {
        is PostMedia.TextMedia -> TextPostMedia(text = postMedia.text, modifier = modifier)
        is PostMedia.ImageMedia -> ImagePostMedia(modifier = modifier, media = postMedia)
        PostMedia.NoMedia -> Box(modifier = modifier) {}
        is PostMedia.LinkMedia -> StaticLinkPreview(
            url = postMedia.url,
            thumbnail = postMedia.thumbnail,
            modifier = modifier,
        )

        is PostMedia.VideoMedia -> StaticLinkPreview(
            url = postMedia.url,
            thumbnail = postMedia.thumbnail,
            modifier = modifier,
        )
    }
}

@Composable
fun ImagePostMedia(modifier: Modifier, media: PostMedia.ImageMedia) {
    val ratio =
        (if (media.width == 0) 1f else media.width.toFloat() / if (media.height == 0) 1f else media.height.toFloat()).absoluteValue
    Image(
        painter = rememberImagePainter(data = media.url) {
            error(R.drawable.ic_image_error_24dp)
        },
        contentDescription = stringResource(id = R.string.submission_image),
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .aspectRatio(if (ratio < 1.0) 1f else ratio)
            .clip(RoundedCornerShape(8.dp)),
    )
}

@Composable
fun TextPostMedia(text: String, modifier: Modifier) {
    Box(
        modifier = modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                0.5.dp,
                MaterialTheme.colors.onBackground.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            ),
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = text,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(8.dp),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun PostUiModel.openComments(): Event =
    ScreenNavigationEvent(CommentScreenNavigation.open(subredditName,id))

private fun PostUiModel.openAuthorScreen(): Event =
    ScreenNavigationEvent(UserScreenNavigation.open(author))

private fun PostUiModel.openSubreddit(): Event =
    ScreenNavigationEvent(SubredditScreenNavigation.open(subredditName))

private fun PostUiModel.openPostMedia(): Event =
    when(this.postMedia){
        is PostMedia.ImageMedia -> {
            ToastEvent(toString())
        }
        is PostMedia.LinkMedia -> ScreenNavigationEvent(CommentScreenNavigation.open(subredditName,id))
        PostMedia.NoMedia -> ToastEvent(toString())
        is PostMedia.TextMedia -> ScreenNavigationEvent(CommentScreenNavigation.open(subredditName,id))
        is PostMedia.VideoMedia -> ScreenNavigationEvent(VideoScreenNavigation.open(postMedia.url))
    }