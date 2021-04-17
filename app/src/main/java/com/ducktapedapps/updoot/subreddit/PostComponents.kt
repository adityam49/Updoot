package com.ducktapedapps.updoot.subreddit

import android.util.Log
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.transform.CircleCropTransformation
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.common.*
import com.ducktapedapps.updoot.theme.StickyPostColor
import com.ducktapedapps.updoot.utils.getCompactCountAsString
import com.ducktapedapps.updoot.utils.getCompactDateAsString
import com.google.accompanist.coil.CoilImage
import com.google.accompanist.imageloading.ImageLoadState.*
import com.google.accompanist.imageloading.MaterialLoadingImage
import kotlin.math.absoluteValue


@Composable
fun CompactPost(
    post: PostUiModel,
    onClickMedia: () -> Unit,
    onClickPost: () -> Unit,
    openSubreddit: (String) -> Unit,
    openUser: (String) -> Unit,
) {
    val (optionsDialog, setOptionsDialogVisibility) = remember { mutableStateOf(false) }

    val list = listOf(
        MenuItemModel({ setOptionsDialogVisibility(false) }, "Copy Link", R.drawable.ic_link_24dp),
        MenuItemModel({
            setOptionsDialogVisibility(false)
            openSubreddit(post.subredditName)
        }, post.subredditName, R.drawable.ic_subreddit_default_24dp),
        MenuItemModel({
            setOptionsDialogVisibility(false)
            openUser(post.author)
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
                onClick = onClickPost,
                onLongClick = { setOptionsDialogVisibility(true) })
    ) {
        val (mediaThumbnail, title, metaData, voteCounter, gildings) = createRefs()
        CompactMediaThumbnail(
            post = post,
            modifier = Modifier
                .clip(CircleShape)
                .requiredSize(48.dp)
                .clickable(onClick = onClickMedia)
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
            is Thumbnail.Remote -> CoilImage(
                data = post.thumbnail.url,
                modifier = modifier,
                requestBuilder = {
                    transformations(CircleCropTransformation())
                }
            ) { imageLoadState ->
                when (imageLoadState) {
                    is Success -> Image(
                        painter = imageLoadState.painter,
                        contentDescription = "Post Thumbnail"
                    )
                    is Error -> Image(
                        painterResource(id = post.thumbnail.fallbackLocalThumbnail),
                        contentDescription = "Error Icon",
                        modifier = modifier
                    )
                    else -> Unit
                }
            }

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
    onClickMedia: () -> Unit,
    openPost: () -> Unit,
    openSubreddit: (String) -> Unit,
    openUser: (String) -> Unit,
) {
    val (optionsDialog, setOptionsDialogVisibility) = remember { mutableStateOf(false) }

    val list = listOf(
        MenuItemModel({ setOptionsDialogVisibility(false) }, "Copy Link", R.drawable.ic_link_24dp),
        MenuItemModel({
            setOptionsDialogVisibility(false)
            openSubreddit(post.subredditName)
        }, post.subredditName, R.drawable.ic_subreddit_default_24dp),
        MenuItemModel({
            setOptionsDialogVisibility(false)
            openUser(post.author)
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
                onClick = openPost,
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
                    onClick = onClickMedia,
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
    val ratio = (media.width.toFloat() / media.height.toFloat()).absoluteValue
    Log.i("AspectRatio", "ratio :$ratio")
    CoilImage(
        data = media.url,
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .aspectRatio(if (ratio < 1.0) 1f else ratio)
            .clip(RoundedCornerShape(8.dp)),
    ) { imageLoadState ->
        when (imageLoadState) {
            Empty -> Unit
            Loading -> Unit
            is Success -> MaterialLoadingImage(
                result = imageLoadState,
                fadeInEnabled = true,
                contentDescription = "Post Image"
            )
            is Error -> Icon(
                painter = painterResource(id = R.drawable.ic_image_error_24dp),
                modifier = modifier,
                contentDescription = "Error Icon"
            )

        }
    }
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