package com.ducktapedapps.updoot.ui.comments

import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.ExperimentalLazyDsl
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asFlow
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.data.local.model.Comment
import com.ducktapedapps.updoot.data.local.model.LinkData
import com.ducktapedapps.updoot.ui.comments.SubmissionContent.*
import com.ducktapedapps.updoot.ui.comments.SubmissionContent.LinkState.*
import com.ducktapedapps.updoot.ui.theme.StickyPostColor
import dev.chrisbanes.accompanist.glide.GlideImage

@ExperimentalLazyDsl
@Composable
fun CommentsScreen(viewModel: CommentsVM, openContent: (SubmissionContent) -> Unit) {
    val loading = viewModel.isLoading.asFlow().collectAsState(initial = true)
    val content = viewModel.content.collectAsState(initial = null)
    val linkData = viewModel.submissionData.collectAsState(initial = null)
    val allComments = viewModel.allComments.collectAsState(initial = emptyList())
    LazyColumn {
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
                        singleThreadMode = false, // TODO : get value from preference storage
                        threadSpacingWidth = 6.dp,
                        threadWidth = 2.dp,
                )
                is Comment.MoreCommentData -> MoreComment(
                        data = comment,
                        loadMoreComments = { viewModel.loadMoreComment(comment, index) },
                        singleThreadMode = false, // TODO : get value from preference storage
                        threadSpacingWidth = 6.dp,
                        threadWidth = 2.dp,
                )
            }
        }

        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun Content(content: SubmissionContent, onClick: (SubmissionContent) -> Unit) {
    when (content) {
        is Image -> ContentImage(image = content, openImage = { onClick(content) })
        is Video -> Unit
        is SelfText -> TextPost(text = content.parsedMarkdown.toString())
        is LoadedLink,
        is LoadingLink,
        is NoMetaDataLink -> LinkPreview(linkState = content as LinkState, openLink = { onClick(content) })
        JustTitle -> Unit
    }
}

@Composable
fun LinkPreview(linkState: LinkState, openLink: () -> Unit) {
    Card(
            Modifier
                    .fillMaxWidth().wrapContentHeight()
                    .padding(start = 16.dp, end = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = openLink),
    ) {
        val imageModifier = Modifier.size(64.dp).padding(start = 8.dp, end = 8.dp)
        Row(modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(8.dp)) {
            when (linkState) {
                is LoadingLink -> LoadingLinkPreview(loadingLink = linkState, imageModifier = imageModifier)
                is LoadedLink -> LoadedLinkPreview(linkState = linkState, imageModifier = imageModifier)
                is NoMetaDataLink -> NoMetaDataLinkPreview(linkState = linkState, imageModifier = imageModifier)
            }
        }
    }
}

@Composable
fun LoadingLinkPreview(loadingLink: LoadingLink, imageModifier: Modifier) {
    CircularProgressIndicator(modifier = imageModifier)
    ProvideEmphasis(emphasis = AmbientEmphasisLevels.current.disabled) {
        Text(text = loadingLink.url, style = MaterialTheme.typography.caption)
    }
}

@Composable
fun LoadedLinkPreview(linkState: LoadedLink, imageModifier: Modifier) {
    GlideImage(
            data = linkState.linkModel.image ?: "",
            modifier = imageModifier,
            requestBuilder = { circleCrop() },
            loading = { CircularProgressIndicator(modifier = imageModifier) }
    )
    Column {
        ProvideEmphasis(emphasis = AmbientEmphasisLevels.current.medium) {
            Text(text = linkState.linkModel.title
                    ?: "", style = MaterialTheme.typography.body2)
        }
        ProvideEmphasis(emphasis = AmbientEmphasisLevels.current.disabled) {
            Text(text = linkState.linkModel.description
                    ?: "", style = MaterialTheme.typography.caption, maxLines = 3)
        }
        ProvideEmphasis(emphasis = AmbientEmphasisLevels.current.disabled) {
            Text(text = linkState.linkModel.siteName, style = MaterialTheme.typography.caption)
        }
    }
}

@Composable
fun NoMetaDataLinkPreview(linkState: NoMetaDataLink, imageModifier: Modifier) {
    Icon(asset = vectorResource(id = R.drawable.ic_link_24dp), modifier = imageModifier)
    Column {
        ProvideEmphasis(emphasis = AmbientEmphasisLevels.current.medium) {
            Text(text = linkState.url, style = MaterialTheme.typography.caption)
        }
        ProvideEmphasis(emphasis = AmbientEmphasisLevels.current.disabled) {
            Text(color = MaterialTheme.colors.error,
                    text = linkState.errorReason,
                    style = MaterialTheme.typography.caption)
        }
    }

}

@Composable
fun TextPost(text: String) {
    Surface(
            modifier = Modifier
                    .fillMaxWidth().wrapContentSize()
                    .padding(8.dp),
            shape = RoundedCornerShape(8.dp),
            content = { Text(text = text, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.body2) }
    )
}

@Composable
fun ContentImage(image: Image, openImage: () -> Unit) {
    val data = image.data.imageData
    GlideImage(
            data = data?.lowResUrl!!,
            modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(
                            (data.lowResWidth ?: 0) / (data.lowResHeight ?: 1).toFloat()
                    ).clickable(onClick = openImage)
    )
}

@Composable
fun Header(linkData: LinkData) {
    Text(
            color = if (linkData.stickied) MaterialTheme.colors.StickyPostColor else MaterialTheme.colors.onBackground,
            text = linkData.title,
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(8.dp)
    )
}
