package com.ducktapedapps.updoot.ui.common

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.ui.comments.SubmissionContent
import com.ducktapedapps.updoot.ui.comments.SubmissionContent.LinkState.*
import dev.chrisbanes.accompanist.glide.GlideImage

@Composable
fun StaticLinkPreview(
        modifier: Modifier = Modifier,
        url: String,
        thumbnail: String?,
        onClickLink: () -> Unit
) {
    Row(modifier = modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colors.surface.copy(alpha = 0.5f))
            .clickable(onClick = onClickLink),
            verticalAlignment = Alignment.Top
    ) {
        GlideImage(
                data = thumbnail ?: "",
                error = { vectorResource(id = R.drawable.ic_image_error_24dp) },
                requestBuilder = {
                    centerCrop().circleCrop()
                },
                modifier = Modifier.size(48.dp).padding(8.dp)
        )
        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Uri.parse(url).authority?.run { Text(text = this, style = MaterialTheme.typography.h6) }
            Providers(AmbientContentAlpha provides ContentAlpha.medium) {
                Text(text = url, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.caption)
            }
        }
    }
}

@Composable
fun LinkPreview(modifier: Modifier = Modifier, linkState: SubmissionContent.LinkState, openLink: () -> Unit) {
    Row(modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colors.surface.copy(alpha = 0.5f))
            .clickable(onClick = openLink)
            .padding(8.dp),
            verticalAlignment = Alignment.Top
    ) {
        val imageModifier = Modifier.padding(end = 16.dp).size(48.dp)
        when (linkState) {
            is LoadingLink -> LoadingLinkPreview(loadingLink = linkState, imageModifier = imageModifier)
            is LoadedLink -> LoadedLinkPreview(linkState = linkState, imageModifier = imageModifier)
            is NoMetaDataLink -> NoMetaDataLinkPreview(linkState = linkState, imageModifier = imageModifier)
        }
        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
fun LoadingLinkPreview(loadingLink: LoadingLink, imageModifier: Modifier) {
    CircularProgressIndicator(modifier = imageModifier)
    Providers(AmbientContentAlpha provides ContentAlpha.high) {
        Text(text = loadingLink.url, style = MaterialTheme.typography.caption)
    }
}

@Composable
fun LoadedLinkPreview(linkState: LoadedLink, imageModifier: Modifier) {
    GlideImage(
            data = linkState.linkModel.image ?: "",
            error = { vectorResource(id = R.drawable.ic_image_error_24dp) },
            modifier = imageModifier,
            requestBuilder = { circleCrop() },
            loading = { CircularProgressIndicator(modifier = imageModifier) }
    )
    Column {
        Providers(AmbientContentAlpha provides ContentAlpha.high) {
            Text(text = linkState.linkModel.title
                    ?: "", style = MaterialTheme.typography.body2)
        }
        Providers(AmbientContentAlpha provides ContentAlpha.medium) {
            Text(text = linkState.linkModel.description
                    ?: "", style = MaterialTheme.typography.caption, maxLines = 3)
        }
        Providers(AmbientContentAlpha provides ContentAlpha.disabled) {
            Text(text = linkState.linkModel.siteName, style = MaterialTheme.typography.caption)
        }
    }
}

@Composable
fun NoMetaDataLinkPreview(linkState: NoMetaDataLink, imageModifier: Modifier) {
    Icon(asset = vectorResource(id = R.drawable.ic_link_24dp), modifier = imageModifier)
    Column {
        Providers(AmbientContentAlpha provides ContentAlpha.high) {
            Text(text = linkState.url, style = MaterialTheme.typography.caption)
        }
        Providers(AmbientContentAlpha provides ContentAlpha.disabled) {
            Text(color = MaterialTheme.colors.error,
                    text = linkState.errorReason,
                    style = MaterialTheme.typography.caption)
        }
    }
}