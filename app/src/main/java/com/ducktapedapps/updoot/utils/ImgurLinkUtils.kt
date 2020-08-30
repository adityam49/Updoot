package com.ducktapedapps.updoot.utils

import android.net.Uri
import com.ducktapedapps.updoot.utils.ImgurResult.*

sealed class ImgurResult {
    data class Image(val url: String) : ImgurResult()
    data class Video(val url: String) : ImgurResult()
    object NonImgurResource : ImgurResult()
    //TODO data class Album(val url : String) :ImgurResult()
}

@Throws(Exception::class)
fun Uri.getImgurMedia(): ImgurResult {
    return if (authority?.contains("imgur") == true) {
        when {
            path?.contains("jpg") == true -> {
                Image(toString())
            }
            path?.contains("gifv") == true -> {
                Video(toString().replace("gifv", "mp4"))
            }
            else -> {
                throw Exception("Unsupported imgur url : ${toString()}")
            }
        }
    } else NonImgurResource
}