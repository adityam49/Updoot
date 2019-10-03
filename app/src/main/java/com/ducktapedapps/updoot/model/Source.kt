package com.ducktapedapps.updoot.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Source(@field:SerializedName("url") val _url: String,
             val height: Int,
             val width: Int) : Serializable {

    val url: String get() = _url.replace("&amp;s", "&s")
}

