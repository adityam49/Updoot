package com.ducktapedapps.updoot.ui.subreddit

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Text
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AmbientEmphasisLevels
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideEmphasis
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.data.local.model.LinkData
import com.ducktapedapps.updoot.ui.common.AllGildings
import com.ducktapedapps.updoot.ui.common.VoteCounter
import com.ducktapedapps.updoot.ui.theme.StickyPostColor
import com.ducktapedapps.updoot.utils.Media.*
import com.ducktapedapps.updoot.utils.getCompactCountAsString
import com.ducktapedapps.updoot.utils.getCompactDateAsString
import com.ducktapedapps.updoot.utils.toMedia
import dev.chrisbanes.accompanist.glide.GlideImage
import dev.chrisbanes.accompanist.imageloading.ImageLoadState.*
import dev.chrisbanes.accompanist.imageloading.MaterialLoadingImage
import kotlin.math.absoluteValue


@Composable
fun CompactPost(
        linkData: LinkData,
        onClickMedia: () -> Unit,
        onClickPost: () -> Unit,
        openOptions: () -> Unit
) {
    ConstraintLayout(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(onClick = onClickPost, onLongClick = openOptions)
    ) {
        val (mediaThumbnail, title, metaData, voteCounter, gildings) = createRefs()
        CompactMediaThumbnail(
                linkData = linkData,
                modifier = Modifier
                        .clip(CircleShape)
                        .size(48.dp)
                        .clickable(onClick = onClickMedia)
                        .constrainAs(mediaThumbnail) {
                            start.linkTo(parent.start, margin = 8.dp)
                            top.linkTo(parent.top, margin = 8.dp)
                            end.linkTo(title.start)
                        })
        SubmissionTitle(
                title = linkData.title,
                isSticky = linkData.stickied,
                modifier = Modifier.constrainAs(title) {
                    start.linkTo(mediaThumbnail.end, 8.dp)
                    top.linkTo(parent.top, margin = 8.dp)
                    end.linkTo(voteCounter.start, margin = 8.dp)
                    bottom.linkTo(metaData.top, margin = 8.dp)
                    height = Dimension.wrapContent
                    width = Dimension.fillToConstraints
                })

        MetaData(linkData = linkData, modifier = Modifier.constrainAs(metaData) {
            start.linkTo(title.start)
            top.linkTo(title.bottom, margin = 8.dp)
            bottom.linkTo(parent.bottom, margin = 8.dp)
            end.linkTo(gildings.start, margin = 8.dp)
            width = Dimension.fillToConstraints
            height = Dimension.wrapContent
        })
        VoteCounter(
                ups = linkData.ups,
                likes = linkData.likes,
                modifier = Modifier.constrainAs(voteCounter) {
                    top.linkTo(parent.top, margin = 8.dp)
                    end.linkTo(parent.end, margin = 8.dp)
                    start.linkTo(title.end)
                    width = Dimension.wrapContent
                }
        )
        AllGildings(gildings = linkData.gildings, modifier = Modifier.constrainAs(gildings) {
            end.linkTo(parent.end, margin = 8.dp)
            top.linkTo(metaData.top)
            bottom.linkTo(metaData.bottom)
        })
    }
}

@Composable
fun CompactMediaThumbnail(linkData: LinkData, modifier: Modifier) {
    GlideImage(
            data = when (linkData.toMedia()) {
                is SelfText -> R.drawable.ic_selftext_24dp
                is Image, is Video -> linkData.thumbnail
                is Link, JustTitle -> R.drawable.ic_link_24dp
            },
            modifier = modifier,
            requestBuilder = { fitCenter().circleCrop() }

    )
}

@Composable
fun SubmissionTitle(
        title: String,
        isSticky: Boolean,
        modifier: Modifier = Modifier
) {
    ProvideEmphasis(emphasis = AmbientEmphasisLevels.current.high) {
        Text(
                text = title,
                color = if (isSticky) MaterialTheme.colors.StickyPostColor else MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.h4,
                modifier = modifier
        )
    }
}

@Composable
private fun MetaData(linkData: LinkData, modifier: Modifier) {
    ProvideEmphasis(emphasis = AmbientEmphasisLevels.current.disabled) {
        Text(
                style = MaterialTheme.typography.h6,
                text = "${linkData.subredditName} • ${getCompactCountAsString(linkData.commentsCount.toLong())} Replies • ${getCompactDateAsString(linkData.created)}",
                modifier = modifier
        )
    }
}

@Composable
fun LargePost(
        linkData: LinkData,
        onClickMedia: () -> Unit,
        openPost: () -> Unit,
        openOptions: () -> Unit
) {
    ConstraintLayout(modifier = Modifier.fillMaxWidth().wrapContentHeight().clickable(onClick = openPost, onLongClick = openOptions)) {
        val (media, title, metaData, voteCounter, gildings) = createRefs()
        SubmissionTitle(
                title = linkData.title,
                isSticky = linkData.stickied,
                modifier = Modifier.constrainAs(title) {
                    start.linkTo(parent.start, margin = 8.dp)
                    top.linkTo(parent.top, margin = 8.dp)
                    end.linkTo(voteCounter.start, margin = 8.dp)
                    bottom.linkTo(media.top, margin = 8.dp)
                    height = Dimension.wrapContent
                    width = Dimension.fillToConstraints
                })

        LargePostMedia(
                linkData = linkData,
                modifier = Modifier.constrainAs(media) {
                    start.linkTo(parent.start)
                    top.linkTo(title.bottom)
                    end.linkTo(parent.end)
                    bottom.linkTo(metaData.top)
                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                }, onClickMedia = onClickMedia)

        MetaData(linkData = linkData, modifier = Modifier.constrainAs(metaData) {
            start.linkTo(media.start, margin = 8.dp)
            top.linkTo(media.bottom, margin = 8.dp)
            bottom.linkTo(parent.bottom, margin = 16.dp)
            width = Dimension.fillToConstraints
            height = Dimension.wrapContent
        })
        VoteCounter(
                ups = linkData.ups,
                likes = linkData.likes,
                modifier = Modifier.constrainAs(voteCounter) {
                    top.linkTo(parent.top, margin = 8.dp)
                    end.linkTo(parent.end, margin = 8.dp)
                    start.linkTo(title.end)
                    width = Dimension.wrapContent
                }
        )
        AllGildings(gildings = linkData.gildings, modifier = Modifier.constrainAs(gildings) {
            end.linkTo(parent.end, margin = 8.dp)
            top.linkTo(metaData.top)
            bottom.linkTo(metaData.bottom)
        })
    }
}

@Composable
fun LargePostMedia(linkData: LinkData, onClickMedia: () -> Unit, modifier: Modifier) {
    when (val mediaData = linkData.toMedia()) {
        is SelfText -> TextPostMedia(text = mediaData.text, modifier = modifier)
        is Image -> ImagePostMedia(onClickMedia = onClickMedia, modifier = modifier, media = mediaData)
        is Video,
        is Link,
        JustTitle -> LinkPreview(linkData = linkData, modifier = modifier, onClickMedia = onClickMedia)
    }
}

@Composable
fun ImagePostMedia(onClickMedia: () -> Unit, modifier: Modifier, media: Image) {
    val data = media.imageData
    val ratio = ((data?.lowResWidth?.toFloat() ?: 1f) / (data?.lowResHeight?.toFloat()
            ?: 1f)).absoluteValue
    Log.i("AspectRatio", "ratio :$ratio")


    GlideImage(
            data = data?.lowResUrl ?: "",
            modifier = modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .aspectRatio(ratio)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onClickMedia)
    ) { imageLoadState ->
        when (imageLoadState) {
            Empty -> Unit
            Loading -> Unit
            is Success -> MaterialLoadingImage(result = imageLoadState, fadeInEnabled = true)
            is Error -> Icon(asset = vectorResource(id = R.drawable.ic_image_error_24dp), modifier = modifier)
        }
    }
}

@Composable
fun TextPostMedia(text: String, modifier: Modifier) {
    Box(modifier = modifier
            .padding(8.dp)
            .border(
                    width = 1.dp,
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
            )
    ) {
        Text(text = text, style = MaterialTheme.typography.body2, modifier = Modifier.padding(8.dp), maxLines = 3)
    }
}

@Composable
private fun LinkPreview(linkData: LinkData, modifier: Modifier, onClickMedia: () -> Unit) {
    Row(modifier = modifier
            .padding(8.dp)
            .border(
                    width = 1.dp,
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
            ).clickable(onClick = onClickMedia),
            verticalAlignment = Alignment.Top
    ) {
        GlideImage(
                data = linkData.thumbnail,
                error = { Icon(asset = vectorResource(id = R.drawable.ic_link_24dp)) },
                requestBuilder = {
                    centerCrop().circleCrop()
                },
                modifier = Modifier.size(48.dp).padding(8.dp)
        )
        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Uri.parse(linkData.url).authority?.run { Text(text = this, style = MaterialTheme.typography.h5) }
            ProvideEmphasis(emphasis = AmbientEmphasisLevels.current.disabled) {
                Text(text = linkData.url, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.caption)
            }
        }
    }
}