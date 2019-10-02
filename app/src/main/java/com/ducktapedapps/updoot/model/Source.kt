package com.ducktapedapps.updoot.model

import com.google.gson.annotations.SerializedName

class Source(@field:SerializedName("url") val _url: String,
             val height: Int,
             val width: Int) {

    val url: String get() = _url.replace("&amp;s", "&s")
}

