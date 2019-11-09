package com.ducktapedapps.updoot.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Resolution(@field:SerializedName("url") val _url: String,
                      val width: Int,
                      val height: Int) : Serializable {

    val url: String get() = _url.replace("&amp;s", "&s")
}