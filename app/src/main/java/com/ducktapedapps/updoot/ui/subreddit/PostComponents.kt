package com.ducktapedapps.updoot.ui.subreddit

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.loadVectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.data.local.model.LinkData
import com.ducktapedapps.updoot.ui.common.AllGildings
import com.ducktapedapps.updoot.ui.common.StaticLinkPreview
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
                        .preferredSize(48.dp)
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
    when (linkData.toMedia()) {
        is Image, is Video -> GlideImage(
                data = linkData.thumbnail,
                error = {
                    it.throwable.printStackTrace()
                    loadVectorResource(id = R.drawable.ic_image_error_24dp).resource.resource?.let { imageVector ->
                        Image(imageVector = imageVector, modifier = modifier)
                    }
                },
                modifier = modifier,
                requestBuilder = { fitCenter().circleCrop() }
        )
        is SelfText -> loadVectorResource(id = R.drawable.ic_selftext_24dp).resource.resource?.let {
            Image(imageVector = it, modifier = modifier)
        }

        is Link, JustTitle -> loadVectorResource(id = R.drawable.ic_link_24dp).resource.resource?.let {
            Image(imageVector = it, modifier = modifier)
        }

    }
}

@Composable
fun SubmissionTitle(
        title: String,
        isSticky: Boolean,
        modifier: Modifier = Modifier
) {
    Providers(AmbientContentAlpha provides ContentAlpha.high) {
        Text(
                text = title,
                color = if (isSticky) MaterialTheme.colors.StickyPostColor else MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.h5,
                modifier = modifier
        )
    }
}

@Composable
private fun MetaData(linkData: LinkData, modifier: Modifier) {
    Providers(AmbientContentAlpha provides ContentAlpha.disabled) {
        Text(
                style = MaterialTheme.typography.caption,
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
                    top.linkTo(title.top)
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
        JustTitle -> StaticLinkPreview(
                url = linkData.url,
                thumbnail = linkData.thumbnail,
                modifier = modifier,
                onClickLink = onClickMedia
        )
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
                    .aspectRatio(if (ratio < 1.0) 1f else ratio)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onClickMedia)
    ) { imageLoadState ->
        when (imageLoadState) {
            Empty -> Unit
            Loading -> Unit
            is Success -> MaterialLoadingImage(result = imageLoadState, fadeInEnabled = true)
            is Error -> loadVectorResource(id = R.drawable.ic_image_error_24dp).resource.resource?.let {
                Icon(it, modifier = modifier)
            }
        }
    }
}

@Composable
fun TextPostMedia(text: String, modifier: Modifier) {
    Box(
            modifier = modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colors.surface.copy(alpha = 0.5f))
    ) {
        Providers(AmbientContentAlpha provides ContentAlpha.medium) {
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