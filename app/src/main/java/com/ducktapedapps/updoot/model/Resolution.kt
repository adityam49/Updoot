package com.ducktapedapps.updoot.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Resolution(@field:SerializedName("url") val _url: String,
                      val width: Int,
                      val height: Int) : Parcelable {

    val url: String get() = _url.replace("&amp;s", "&s")
}