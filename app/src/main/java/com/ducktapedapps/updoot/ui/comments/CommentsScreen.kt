package com.ducktapedapps.updoot.ui.comments

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.loadVectorResource
import androidx.compose.ui.unit.dp
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.data.local.model.FullComment
import com.ducktapedapps.updoot.data.local.model.MoreComment
import com.ducktapedapps.updoot.ui.common.StaticLinkPreview
import com.ducktapedapps.updoot.ui.subreddit.PostMedia
import com.ducktapedapps.updoot.ui.subreddit.PostMedia.*
import com.ducktapedapps.updoot.ui.subreddit.PostUiModel
import com.ducktapedapps.updoot.ui.theme.StickyPostColor
import dev.chrisbanes.accompanist.glide.GlideImage
import dev.chrisbanes.accompanist.imageloading.ImageLoadState

@Composable
fun CommentsScreen(viewModel: ICommentsVM, openContent: (PostMedia) -> Unit) {
    val loading = viewModel.isLoading.collectAsState()
    val linkData = viewModel.post.collectAsState()
    val allComments = viewModel.comments.collectAsState()
    val singleThreadMode = viewModel.singleThreadMode.collectAsState()


//TODO : val singleColorThread = viewModel.singleColorThreadMode.collectAsState()
    val listState = rememberLazyListState()
    Surface(color = MaterialTheme.colors.background) {
        LazyColumn(state = listState) {
            if (loading.value) item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            linkData.value?.run {
                item {
                    Header(post = this@run)
                }
                item {
                    Content(content = postMedia, onClick = openContent)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            itemsIndexed(items = allComments.value) { index, comment ->
                when (comment) {
                    is FullComment -> FullComment(
                            comment = comment,
                            onClickComment = { viewModel.toggleChildrenVisibility(index) },
                            singleThreadMode = singleThreadMode.value,
                            threadSpacingWidth = 6.dp,
                            threadWidth = 2.dp,
                    )
                    is MoreComment -> MoreComment(
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
fun Content(content: PostMedia, onClick: (PostMedia) -> Unit) {
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
fun TextPost(text: String) {
    Box(
            modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .border(0.5.dp, MaterialTheme.colors.onBackground, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp)),
            content = {
                Providers(AmbientContentAlpha provides ContentAlpha.medium) {
                    Text(text = text, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.body2)
                }
            }
    )
}

@Composable
fun ContentImage(image: ImageMedia, openImage: () -> Unit) {
    GlideImage(
            data = image.url,
            modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .aspectRatio(image.width / image.height.toFloat())
                    .clickable(onClick = openImage),
            requestBuilder = {
                transform(RoundedCorners(8))
            }
    ) { imageLoadState ->
        when (imageLoadState) {
            is ImageLoadState.Success -> Image(painter = imageLoadState.painter, contentDescription = "Post Image")
            is ImageLoadState.Error -> loadVectorResource(id = R.drawable.ic_image_error_24dp).resource.resource?.let {
                Icon(it, "Error Icon")
            }
            else -> Unit
        }
    }
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
