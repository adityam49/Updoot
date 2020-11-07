package com.ducktapedapps.updoot.utils

import com.ducktapedapps.updoot.data.local.model.ImageVariants
import com.ducktapedapps.updoot.data.local.model.LinkData
import com.ducktapedapps.updoot.utils.Media.*
import java.net.URI

sealed class Media {
    data class SelfText(val text: String) : Media()
    data class Image(val imageData: ImageVariants?) : Media()
    data class Video(val url: String) : Media()
    data class Link(val url: String) : Media()
    object JustTitle : Media()
}

fun LinkData.toMedia(): Media = with(URI.create(url)) {
    when {
        authority == null -> JustTitle
        authority.contains("reddit") ->
            if (!selftext.isNullOrBlank()) SelfText(selftext)
            else JustTitle
        authority.contains("imgur") -> getImgurMedia()
        authority.contains(".redd.") -> getRedditMedia()
        else -> Link(toString())
    }
}

private fun LinkData.getImgurMedia(): Media = with(URI.create(url)) {
    when {
        path.hasImageExtension() -> Image(this@getImgurMedia.preview)
        path.hasVideoExtension() -> Video(url.replace(".gifv", ".mp4"))
        else -> if (video != null) Video(video.dash_url) else Link(url)
    }
}

private fun LinkData.getRedditMedia(): Media = with(URI.create(url)) {
    when {
        path.hasImageExtension() -> Image(preview)
        authority.startsWith("v.") -> Video(video?.dash_url ?: url)
        else -> Link(url)
    }
}

private fun String.hasImageExtension(): Boolean =
        endsWith("jpg", true)
                || endsWith("jpeg", true)
                || endsWith("png", true)
                || endsWith("gif", true)

private fun String.hasVideoExtension(): Boolean =
        endsWith("mp4", true) || endsWith("gifv", true)
