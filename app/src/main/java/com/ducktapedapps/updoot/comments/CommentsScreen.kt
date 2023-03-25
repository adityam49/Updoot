package com.ducktapedapps.updoot.comments

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ducktapedapps.navigation.Event
import com.ducktapedapps.navigation.NavigationDirections
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.common.StaticLinkPreview
import com.ducktapedapps.updoot.data.local.model.FullComment
import com.ducktapedapps.updoot.data.local.model.MoreComment
import com.ducktapedapps.updoot.subreddit.PostMedia
import com.ducktapedapps.updoot.subreddit.PostMedia.*
import com.ducktapedapps.updoot.subreddit.PostUiModel
import com.ducktapedapps.updoot.theme.StickyPostColor

@Composable
fun CommentsScreen(
    subredditId: String,
    postId: String,
    publishEvent: (Event) -> Unit
) {
    val viewModel: CommentsVM = hiltViewModel<CommentsVMImpl>().apply {
        setPageKey(subredditId, postId)

    }
    val viewState = viewModel.viewState.collectAsState()
    CommentsScreen(
        viewState = viewState.value,
        publishEvent = publishEvent,
        toggleChildrenVisibility = viewModel::toggleChildrenVisibility,
        loadMoreComment = viewModel::loadMoreComment
    )
}

@Composable
private fun CommentsScreen(
    viewState: ViewState,
    publishEvent: (Event) -> Unit,
    toggleChildrenVisibility: (Int) -> Unit,
    loadMoreComment: (comment: MoreComment, index: Int) -> Unit,
) {
//TODO : val singleColorThread = viewModel.singleColorThreadMode.collectAsState()
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (viewState.isLoading) item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
        viewState.post?.run {
            item {
                Header(post = this@run)
            }
            item {
                Content(content = postMedia, onClick = { media -> publishEvent( media.open()) })
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        itemsIndexed(items = viewState.comments) { index, comment ->
            when (comment) {
                is FullComment -> FullComment(
                    comment = comment,
                    onClickComment = { toggleChildrenVisibility(index) },
                    singleThreadMode = viewState.isSingleThreadMode,
                    threadSpacingWidth = 6.dp,
                    threadWidth = 2.dp,
                )
                is MoreComment -> MoreComment(
                    data = comment,
                    loadMoreComments = { loadMoreComment(comment, index) },
                    singleThreadMode = viewState.isSingleThreadMode,
                    threadSpacingWidth = 6.dp,
                    threadWidth = 2.dp,
                )
            }
        }
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
private fun Content(content: PostMedia, onClick: (PostMedia) -> Unit) {
    when (content) {
        is TextMedia -> TextPost(text = content.text)
        is ImageMedia -> ContentImage(image = content, openImage = { onClick(content) })
        is LinkMedia -> StaticLinkPreview(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick(content) },
            thumbnail = content.thumbnail,
            url = content.url
        )

        is VideoMedia -> Unit
        NoMedia -> Unit
    }
}

@Composable
private fun TextPost(text: String) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .border(0.5.dp, MaterialTheme.colors.onBackground, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp)),
        content = {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = text,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.body2
                )
            }
        }
    )
}

@Composable
fun ContentImage(image: ImageMedia, openImage: () -> Unit) {
    AsyncImage(
        model = image.url,
        error = painterResource(id = R.drawable.ic_image_error_24dp),
        contentDescription = stringResource(R.string.submission_image),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .aspectRatio(image.width / image.height.toFloat())
            .clip(shape = RoundedCornerShape(8.dp))
            .clickable(onClick = openImage)
    )
}

@Composable
fun Header(post: PostUiModel) {
    Text(
        color = if (post.isSticky) MaterialTheme.colors.StickyPostColor else MaterialTheme.colors.onBackground,
        text = post.title,
        style = MaterialTheme.typography.h5,
        modifier = Modifier.padding(8.dp)
    )
}
