package com.ducktapedapps.updoot.common

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ducktapedapps.updoot.subreddit.Thumbnail

@Composable
fun StaticLinkPreview(
    modifier: Modifier = Modifier,
    url: String,
    thumbnail: Thumbnail,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                0.5.dp,
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            ),
        verticalAlignment = Alignment.Top
    ) {
        when (thumbnail) {
            is Thumbnail.Remote ->
                Icon(
                    painter = painterResource(thumbnail.fallbackLocalThumbnail),
                    contentDescription = "Link preview icon",
                    modifier = Modifier
                    .size(48.dp)
                    .padding(8.dp)
                )

//                CoilImage(
//                data = thumbnail.url,
//                requestBuilder = { transformations(CircleCropTransformation()) },
//                modifier = Modifier
//                    .size(48.dp)
//                    .padding(8.dp)
//            ) { imageLoadState: ImageLoadState ->
//                when (imageLoadState) {
//                    is Success -> Image(
//                        painter = imageLoadState.painter,
//                        contentDescription = "Link thumbnail"
//                    )
//                    is Error -> Image(
//                        painter = painterResource(id = thumbnail.fallbackLocalThumbnail),
//                        "Link preview icon"
//                    )
//
//                    else -> Unit
//                }
//            }
            is Thumbnail.LocalThumbnail -> Image(
                painter = painterResource(id = thumbnail.imageResource),
                "Link preview icon"
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Uri.parse(url).authority?.run { Text(text = this, style = MaterialTheme.typography.headlineLarge) }
                Text(
                    text = url,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium
                )
        }
    }
}

//@Composable
//fun LinkPreview(modifier: Modifier = Modifier, linkState: SubmissionContent.LinkState, openLink: () -> Unit) {
//    Row(modifier = modifier
//            .clip(RoundedCornerShape(8.dp))
//            .border(0.5.dp, MaterialTheme.colors.onBackground.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
//            .clickable(onClick = openLink)
//            .padding(8.dp),
//            verticalAlignment = Alignment.Top
//    ) {
//        val imageModifier = Modifier
//                .padding(end = 16.dp)
//                .size(48.dp)
//        when (linkState) {
//            is LoadingLink -> LoadingLinkPreview(loadingLink = linkState, modifier = imageModifier)
//            is LoadedLink -> LoadedLinkPreview(linkState = linkState, modifier = imageModifier)
//            is NoMetaDataLink -> NoMetaDataLinkPreview(linkState = linkState, modifier = imageModifier)
//        }
//        Spacer(modifier = Modifier.width(16.dp))
//    }
//}
//
//@Composable
//fun LoadingLinkPreview(loadingLink: LoadingLink, modifier: Modifier) {
//    CircularProgressIndicator(modifier = modifier)
//    Providers(AmbientContentAlpha provides ContentAlpha.high) {
//        Text(text = loadingLink.url, style = MaterialTheme.typography.caption)
//    }
//}
//
//@Composable
//fun LoadedLinkPreview(linkState: LoadedLink, modifier: Modifier) {
//    if (linkState.linkModel.image != null)
//        GlideImage(
//                data = linkState.linkModel.image,
//                modifier = modifier,
//                requestBuilder = { circleCrop() },
//        ) { imageLoadState ->
//            when (imageLoadState) {
//                is Success -> Image(painter = imageLoadState.painter, contentDescription = "Link thumbnail")
//                is Loading -> CircularProgressIndicator()
//                is Error -> loadVectorResource(id = R.drawable.ic_link_24dp).resource.resource?.let {
//                    Image(it, "Link icon")
//                }
//                else -> Unit
//            }
//        }
//    else loadVectorResource(id = R.drawable.ic_link_24dp).resource.resource?.let {
//        Image(modifier = modifier, imageVector = it, contentDescription = "Link Icon", contentScale = ContentScale.FillBounds)
//    }
//    Column {
//        Providers(AmbientContentAlpha provides ContentAlpha.high) {
//            Text(text = linkState.linkModel.title
//                    ?: "", style = MaterialTheme.typography.body2)
//        }
//        Providers(AmbientContentAlpha provides ContentAlpha.medium) {
//            Text(text = linkState.linkModel.description
//                    ?: "", style = MaterialTheme.typography.caption, maxLines = 3)
//        }
//        Providers(AmbientContentAlpha provides ContentAlpha.disabled) {
//            Text(text = linkState.linkModel.siteName, style = MaterialTheme.typography.caption)
//        }
//    }
//}
//
//@Composable
//fun NoMetaDataLinkPreview(linkState: NoMetaDataLink, modifier: Modifier) {
//    loadVectorResource(id = R.drawable.ic_link_24dp).resource.resource?.let {
//        Image(imageVector = it, contentDescription = "Link icon", modifier = modifier)
//    }
//    Column {
//        Providers(AmbientContentAlpha provides ContentAlpha.high) {
//            Text(text = linkState.url, style = MaterialTheme.typography.caption)
//        }
//        Providers(AmbientContentAlpha provides ContentAlpha.disabled) {
//            Text(color = MaterialTheme.colors.error,
//                    text = linkState.errorReason,
//                    style = MaterialTheme.typography.caption)
//        }
//    }
//}