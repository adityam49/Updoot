package com.ducktapedapps.updoot.subreddit

import androidx.annotation.DrawableRes
import com.ducktapedapps.navigation.Event
import com.ducktapedapps.navigation.NavigationDirections
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.data.local.model.Gildings
import com.ducktapedapps.updoot.data.local.model.Post
import com.ducktapedapps.updoot.subreddit.PostMedia.*
import com.ducktapedapps.updoot.subreddit.Thumbnail.LocalThumbnail
import com.ducktapedapps.updoot.subreddit.Thumbnail.Remote
import java.net.URI
import java.util.*

data class PostUiModel(
    val id: String,
    val author: String,
    val subredditName: String,
    val title: String,
    val upVotes: Int?,
    val userHasUpVoted: Boolean?,
    val thumbnail: Thumbnail,
    val postMedia: PostMedia,
    val replyCount: Int,
    val creationDate: Date,
    val gildings: Gildings,
    val isNsfw: Boolean,
    val isSticky: Boolean,
)

sealed class Thumbnail {
    data class Remote(val url: String, @DrawableRes val fallbackLocalThumbnail: Int) : Thumbnail()
    data class LocalThumbnail(@DrawableRes val imageResource: Int) : Thumbnail()
}

sealed class PostMedia {
    data class TextMedia(val text: String) : PostMedia()
    data class ImageMedia(
        val url: String,
        val height: Int,
        val width: Int,
    ) : PostMedia()

    data class LinkMedia(val url: String, val thumbnail: Thumbnail) : PostMedia()
    data class VideoMedia(val url: String, val thumbnail: Thumbnail) : PostMedia()
    object NoMedia : PostMedia()

    fun open(): Event {
        return when (this) {
            is ImageMedia -> {
                Event.ScreenNavigationEvent(
                    NavigationDirections.ImageScreenNavigation.open(url)
                )
            }

            is LinkMedia -> Event.OpenWebLink(url)
            is VideoMedia -> Event.ScreenNavigationEvent(
                NavigationDirections.VideoScreenNavigation.open(url)
            )
            else -> Event.ToastEvent(toString())
        }
    }
}


fun Post.toUiModel(): PostUiModel = PostUiModel(
    id = id,
    author = author,
    subredditName = subredditName,
    title = title,
    upVotes = upVotes,
    userHasUpVoted = userHasUpVoted,
    isNsfw = isNsfw,
    thumbnail = getThumbnail(),
    postMedia = toMedia(),
    replyCount = commentsCount,
    creationDate = created,
    gildings = gildings,
    isSticky = stickied,
)


private fun Post.getThumbnail() = if (!postThumbnail.isNullOrBlank())
    Remote(
        url = postThumbnail,
        fallbackLocalThumbnail = localThumbnail().imageResource
    )
else localThumbnail()

private fun Post.localThumbnail() = LocalThumbnail(
    when {
        !mediaText.isNullOrBlank() -> R.drawable.ic_selftext_24dp
        !mediaImage?.lowResUrl.isNullOrBlank() -> R.drawable.ic_image_error_24dp
        !mediaVideo?.dashUrl.isNullOrBlank() -> R.drawable.ic_image_error_24dp
        else -> R.drawable.ic_link_24dp
    }
)


fun Post.toMedia(): PostMedia = with(URI.create(postContentUrl)) {
    when {
        authority == null -> NoMedia
        authority.contains("reddit") ->
            if (!mediaText.isNullOrBlank()) TextMedia(mediaText)
            else NoMedia
        authority.contains("imgur") -> getImgurMedia()
        authority.contains(".redd.") -> getRedditMedia()
        else -> LinkMedia(url = toString(), thumbnail = getThumbnail())
    }
}

private fun Post.getImgurMedia(): PostMedia = with(URI.create(postContentUrl)) {
    when {
        path.hasImageExtension() -> ImageMedia(
            mediaImage?.lowResUrl ?: this.toString(),
            mediaImage?.lowResHeight ?: 0,
            mediaImage?.lowResWidth ?: 0
        )
        path.hasVideoExtension() -> VideoMedia(
            postContentUrl.replace(".gifv", ".mp4"),
            getThumbnail()
        )
        else -> if (!mediaVideo?.dashUrl.isNullOrBlank()) VideoMedia(
            mediaVideo!!.dashUrl!!,
            getThumbnail()
        ) else LinkMedia(postContentUrl, getThumbnail())
    }
}

private fun Post.getRedditMedia(): PostMedia = with(URI.create(postContentUrl)) {
    when {
        path.hasImageExtension() -> ImageMedia(
            mediaImage?.lowResUrl ?: this.toString(),
            mediaImage?.lowResHeight ?: 0,
            mediaImage?.lowResWidth ?: 0
        )
        authority.startsWith("v.") -> VideoMedia(
            url = mediaVideo?.dashUrl ?: postContentUrl,
            thumbnail = getThumbnail()
        )
        else -> LinkMedia(postContentUrl, getThumbnail())
    }
}

private fun String.hasImageExtension(): Boolean =
    endsWith("jpg", true)
            || endsWith("jpeg", true)
            || endsWith("png", true)
            || endsWith("gif", true)

private fun String.hasVideoExtension(): Boolean =
    endsWith("mp4", true) || endsWith("gifv", true)



