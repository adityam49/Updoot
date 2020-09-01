package com.ducktapedapps.updoot.utils

import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.utils.Media.*
import java.net.URI

sealed class Media {
    data class SelfText(val text: String) : Media()
    data class Image(val lowResUrl: String?, val highResUrl: String) : Media()
    data class Video(val url: String) : Media()
    data class Link(val url: String) : Media()
    object JustTitle : Media()
}

fun LinkData.getImgurMedia(): Media = with(URI.create(url)) {
    when {
        (path.run {
            endsWith(".jpg") || endsWith(".png") || endsWith("gif")
        }) -> {
            Image(thumbnail, url)
        }
        path.endsWith(".gifv") -> {
            Video(url.replace(".gifv", ".mp4"))
        }
        else -> if (!videoUrl.isNullOrBlank()) Video(videoUrl) else Link(url)
    }
}

fun LinkData.getRedditMedia(): Media = with(URI.create(url)) {
    when {
        (path.run {
            endsWith(".jpg") || endsWith(".png") || endsWith("gif")
        }) -> Image(thumbnail, url)
        authority.startsWith("v.") -> Video(if (!videoUrl.isNullOrBlank()) videoUrl else url)
        else -> Link(url)
    }
}

fun LinkData.toMedia(): Media = with(URI.create(url)) {
    when {
        authority.contains("reddit") ->
            if (!selftext.isNullOrBlank()) SelfText(selftext)
            else JustTitle
        authority.contains("imgur") -> getImgurMedia()
        authority.contains(".redd.") -> getRedditMedia()
        else -> Link(toString())
    }
}
