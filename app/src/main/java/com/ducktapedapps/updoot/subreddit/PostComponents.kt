package com.ducktapedapps.updoot.subreddit

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ducktapedapps.navigation.Event
import com.ducktapedapps.navigation.Event.ScreenNavigationEvent
import com.ducktapedapps.navigation.Event.ToastEvent
import com.ducktapedapps.navigation.NavigationDirections.CommentScreenNavigation
import com.ducktapedapps.navigation.NavigationDirections.ImageScreenNavigation
import com.ducktapedapps.navigation.NavigationDirections.VideoScreenNavigation
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.common.AllGildings
import com.ducktapedapps.updoot.common.StaticLinkPreview
import com.ducktapedapps.updoot.common.VoteCounter
import com.ducktapedapps.updoot.theme.StickyPostColor
import com.ducktapedapps.updoot.theme.downVoteColor
import com.ducktapedapps.updoot.theme.upVoteColor
import com.ducktapedapps.updoot.utils.getCompactCountAsString


@Composable
fun LargePost(
    modifier: Modifier = Modifier,
    post: PostUiModel,
    isLoggedIn: Boolean,
    doAction: (ScreenAction) -> Unit,
) {
    val boundaryPadding = remember {
        16.dp
    }
    Column(modifier = modifier) {
        SubmissionTitle(
            title = post.title,
            isSticky = post.isSticky,
            modifier = Modifier.padding(
                top = boundaryPadding / 2,
                start = boundaryPadding,
                end = boundaryPadding
            )
        )

        LargePostMedia(
            postMedia = post.postMedia,
            modifier = Modifier
                .padding(horizontal = boundaryPadding, vertical = boundaryPadding / 2)
        )
        FlowRow(
            modifier = Modifier.padding(horizontal = boundaryPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MetaData(post = post, modifier = Modifier.wrapContentSize())
            AllGildings(gildings = post.gildings, modifier = Modifier.wrapContentSize())
        }
        if (isLoggedIn)

            HorizontalVotingOptions(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = boundaryPadding,
                        vertical = 4.dp
                    ),
                postId = post.id,
                doAction = doAction,
                currentVote = post.userHasUpVoted,
                votes = post.upVotes
            )

    }
}

@Composable
fun CompactPost(
    modifier: Modifier = Modifier,
    post: PostUiModel,
    isLoggedIn: Boolean,
    publishEvent: (Event) -> Unit,
    doAction: (ScreenAction) -> Unit,
) {
    val boundaryPadding = remember {
        16.dp
    }

    Row(
        modifier = modifier,
    ) {
        CompactMediaThumbnail(
            post = post,
            modifier = Modifier
                .padding(start = boundaryPadding)
                .clip(CircleShape)
                .size(48.dp)
                .clickable(onClick = { publishEvent(post.openPostMedia()) }),
        )
        Column(
            Modifier
                .weight(1f)
                .padding(start = boundaryPadding, end = boundaryPadding)
        ) {
            SubmissionTitle(
                title = post.title,
                isSticky = post.isSticky,
                modifier = Modifier.fillMaxWidth()
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetaData(post = post, modifier = Modifier.wrapContentSize())
                AllGildings(gildings = post.gildings, modifier = Modifier.wrapContentSize())
            }
        }
        if (isLoggedIn)
            VerticalVotingOptions(
                postId = post.id,
                doAction = doAction,
                currentVote = post.userHasUpVoted,
                votes = post.upVotes,
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(end = boundaryPadding)
            )
    }
}

@Composable
fun HorizontalVotingOptions(
    modifier: Modifier,
    postId: String,
    doAction: (ScreenAction) -> Unit,
    currentVote: Boolean?,
    votes: Int?,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            doAction(
                ScreenAction.UpVote(
                    id = postId,
                    currentVote = currentVote,
                )
            )
        }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_upvote_24dp),
                contentDescription = "DownVote",
                tint = if (currentVote == true) upVoteColor else LocalContentColor.current
            )
        }
        VoteCounter(
            upVotes = votes,
            userHasUpVoted = currentVote,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
        )
        IconButton(onClick = {
            doAction(
                ScreenAction.DownVote(
                    id = postId,
                    currentVote = currentVote,
                )
            )
        }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_downvote_24dp),
                contentDescription = "DownVote",
                tint = if (currentVote == false) downVoteColor else LocalContentColor.current
            )
        }
    }
}

@Composable
fun VerticalVotingOptions(
    modifier: Modifier,
    postId: String,
    doAction: (ScreenAction) -> Unit,
    currentVote: Boolean?,
    votes: Int?,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        Icon(
            modifier = Modifier.clickable {
                doAction(
                    ScreenAction.UpVote(
                        id = postId,
                        currentVote = currentVote,
                    )
                )
            },
            painter = painterResource(id = R.drawable.ic_upvote_24dp),
            contentDescription = "DownVote",
            tint = if (currentVote == true) upVoteColor else LocalContentColor.current
        )

        VoteCounter(
            upVotes = votes,
            userHasUpVoted = currentVote,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Icon(
            modifier = Modifier.clickable {
                doAction(
                    ScreenAction.DownVote(
                        id = postId,
                        currentVote = currentVote,
                    )
                )
            },
            painter = painterResource(id = R.drawable.ic_downvote_24dp),
            contentDescription = "DownVote",
            tint = if (currentVote == false) downVoteColor else LocalContentColor.current
        )

    }
}

@Composable
fun CompactMediaThumbnail(post: PostUiModel, modifier: Modifier) {
    if (post.thumbnail.first() is String)
        AsyncImage(
            model = post.thumbnail.first(),
            contentDescription = "Link preview icon",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    else
        Box(
            modifier = modifier.border(
                2.dp,
                color = if (isSystemInDarkTheme())
                    Color.White.copy(alpha = 0.5f)
                else
                    Color.Black.copy(alpha = 0.5f),
                shape = CircleShape
            )
        ) {
            AsyncImage(
                model = post.thumbnail.first(),
                contentDescription = "Link preview icon",
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Center),
                contentScale = ContentScale.Crop
            )
        }

}

@Composable
fun SubmissionTitle(
    title: String,
    isSticky: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        color = if (isSticky) MaterialTheme.colorScheme.StickyPostColor else MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.headlineLarge,
        modifier = modifier
    )
}

@Composable
private fun MetaData(post: PostUiModel, modifier: Modifier) {
    Row(modifier) {
        Text(
            style = MaterialTheme.typography.labelMedium,
            text = "${post.subredditName} • ${getCompactCountAsString(post.replyCount.toLong())} Replies",
            modifier = Modifier
        )

        if (post.gildings.hasGilding()) {
            Text(
                style = MaterialTheme.typography.labelMedium,
                text = " • ",
                modifier = Modifier
            )
        }
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
    val ratio = remember(media.url) {
        media.width.toFloat() / if (media.height < 1f) 1f else media.height.toFloat()
    }
    AsyncImage(
        model = media.url,
        error = painterResource(id = R.drawable.ic_image_error_24dp),
        contentDescription = stringResource(id = R.string.submission_image),
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(ratio)
            .clip(RoundedCornerShape(8.dp)),
    )
}

@Composable
fun TextPostMedia(text: String, modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                0.5.dp,
                MaterialTheme.colorScheme.onBackground,
                RoundedCornerShape(8.dp)
            ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(8.dp),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun PostUiModel.openComments(): Event =
    ScreenNavigationEvent(CommentScreenNavigation.open(subredditName, id))

private fun PostUiModel.openPostMedia(): Event =
    when (this.postMedia) {
        is PostMedia.ImageMedia -> {
            ScreenNavigationEvent(ImageScreenNavigation.open(this.postMedia.url))
        }

        is PostMedia.LinkMedia -> ScreenNavigationEvent(
            CommentScreenNavigation.open(
                subredditName,
                id
            )
        )

        PostMedia.NoMedia -> ToastEvent(toString())
        is PostMedia.TextMedia -> ScreenNavigationEvent(
            CommentScreenNavigation.open(
                subredditName,
                id
            )
        )

        is PostMedia.VideoMedia -> ScreenNavigationEvent(VideoScreenNavigation.open(postMedia.url))
    }