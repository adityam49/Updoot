package com.ducktapedapps.updoot.model

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
class Source(
        @Json(name = "url") val _url: String,
        val height: Int,
        val width: Int
) : Parcelable {
    val url: String get() = _url.replace("&amp;s", "&s")
}

