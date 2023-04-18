package com.ducktapedapps.updoot.subreddit

import android.webkit.URLUtil
import com.ducktapedapps.navigation.Event
import com.ducktapedapps.navigation.NavigationDirections
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.data.local.model.Gildings
import com.ducktapedapps.updoot.data.local.model.Post
import com.ducktapedapps.updoot.subreddit.PostMedia.*
import java.net.URI
import java.util.*

data class PostUiModel(
    val id: String,
    val author: String,
    val subredditName: String,
    val title: String,
    val upVotes: Int?,
    val userHasUpVoted: Boolean?,
    val thumbnail: List<Any>,
    val postMedia: PostMedia,
    val replyCount: Int,
    val creationDate: Date,
    val gildings: Gildings,
    val isNsfw: Boolean,
    val isSticky: Boolean,
    val isSaved:Boolean,
)

sealed class PostMedia {
    data class TextMedia(val text: String) : PostMedia()
    data class ImageMedia(
        val url: String,
        val height: Int,
        val width: Int,
    ) : PostMedia()

    data class LinkMedia(val url: String, val thumbnail: List<Any>) : PostMedia()
    data class VideoMedia(val url: String, val thumbnail: List<Any>) : PostMedia()
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
    isSaved = saved
)


private fun Post.getThumbnail() = buildList<Any> {
    if (isNsfw) {
        add(R.drawable.ic_nsfw_24dp)
    }
    if (!postThumbnail.isNullOrBlank() && URLUtil.isValidUrl(postThumbnail)) {
        add(postThumbnail)
    }
    when {
        !mediaText.isNullOrBlank() -> add(R.drawable.ic_selftext_24dp)
        !mediaImage?.lowResUrl.isNullOrBlank() -> mediaImage?.lowResUrl?.let { add(it) }
        else -> add(R.drawable.ic_link_24dp)

    }
    add(R.drawable.ic_image_error_24dp)
}

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



