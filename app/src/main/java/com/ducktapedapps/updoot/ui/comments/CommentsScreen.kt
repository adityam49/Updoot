package com.ducktapedapps.updoot.ui.comments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asFlow
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.ducktapedapps.updoot.data.local.model.Comment
import com.ducktapedapps.updoot.data.local.model.LinkData
import com.ducktapedapps.updoot.ui.comments.SubmissionContent.*
import com.ducktapedapps.updoot.ui.comments.SubmissionContent.LinkState.*
import com.ducktapedapps.updoot.ui.common.LinkPreview
import com.ducktapedapps.updoot.ui.theme.StickyPostColor
import dev.chrisbanes.accompanist.glide.GlideImage

@Composable
fun CommentsScreen(viewModel: CommentsVM, openContent: (SubmissionContent) -> Unit) {
    val loading = viewModel.isLoading.asFlow().collectAsState(initial = true)
    val content = viewModel.content.collectAsState(initial = null)
    val linkData = viewModel.submissionData.collectAsState(initial = null)
    val allComments = viewModel.allComments.collectAsState(initial = emptyList())
    val singleThreadMode = viewModel.singleThreadMode.collectAsState()

//TODO : val singleColorThread = viewModel.singleColorThreadMode.collectAsState()

    val listState = rememberLazyListState()
    Surface(color = MaterialTheme.colors.background) {
        LazyColumn(state = listState) {
            if (loading.value) item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            linkData.value?.run {
                item {
                    Header(linkData = this@run)
                }
            }

            content.value?.run {
                item {
                    Content(content = this@run, onClick = openContent)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            itemsIndexed(items = allComments.value) { index, comment ->
                when (comment) {
                    is Comment.CommentData -> FullComment(
                            comment = comment,
                            onClickComment = { viewModel.toggleChildrenVisibility(index) },
                            singleThreadMode = singleThreadMode.value,
                            threadSpacingWidth = 6.dp,
                            threadWidth = 2.dp,
                    )
                    is Comment.MoreCommentData -> MoreComment(
                            data = comment,
                            loadMoreComments = { viewModel.loadMoreComment(comment, index) },
                            singleThreadMode = singleThreadMode.value,
                            threadSpacingWidth = 6.dp,
                            threadWidth = 2.dp,
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun Content(content: SubmissionContent, onClick: (SubmissionContent) -> Unit) {
    when (content) {
        is Image -> ContentImage(image = content, openImage = { onClick(content) })
        is Video -> Unit
        is SelfText -> TextPost(text = content.parsedMarkdown.toString())
        is LinkState -> LinkPreview(
                modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .fillMaxWidth(),
                linkState = content,
                openLink = { onClick(content) }
        )
        JustTitle -> Unit
    }
}

@Composable
fun TextPost(text: String) {
    Surface(
            color = MaterialTheme.colors.surface.copy(alpha = 0.5f),
            modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(8.dp)),
            content = {
                Providers(AmbientContentAlpha provides ContentAlpha.medium) {
                    Text(text = text, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.body2)
                }
            }
    )
}

@Composable
fun ContentImage(image: Image, openImage: () -> Unit) {
    val data = image.data.imageData
    GlideImage(
            data = data?.lowResUrl!!,
            modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .aspectRatio(
                            (data.lowResWidth ?: 0) / (data.lowResHeight ?: 1).toFloat()
                    )
                    .clickable(onClick = openImage),
            requestBuilder = {
                transform(RoundedCorners(8))
            }
    )
}

@Composable
fun Header(linkData: LinkData) {
    Text(
            color = if (linkData.stickied) MaterialTheme.colors.StickyPostColor else MaterialTheme.colors.onBackground,
            text = linkData.title,
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(8.dp)
    )
}
