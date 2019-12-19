package com.ducktapedapps.updoot.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class Source(@field:SerializedName("url") val _url: String,
             val height: Int,
             val width: Int) : Parcelable {

    val url: String get() = _url.replace("&amp;s", "&s")
}

