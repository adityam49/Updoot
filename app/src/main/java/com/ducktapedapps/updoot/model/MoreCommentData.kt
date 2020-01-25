package com.ducktapedapps.updoot.model

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class MoreCommentData(
        val count: Int,
        @Json(name = "name") val _name: String,
        @Json(name = "id") val _id: String,
        @Json(name = "parent_id") val _parent_id: String,
        @Json(name = "depth") val _depth: Int,
        val children: List<String>
) : Parcelable, BaseComment(_id, _depth, _name, _parent_id)