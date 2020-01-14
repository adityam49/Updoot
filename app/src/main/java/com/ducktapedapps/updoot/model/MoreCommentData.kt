package com.ducktapedapps.updoot.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class MoreCommentData(
        val count: Int,
        val name: String,
        val id: String,
        val parent_id: String,
        val depth: Int,
        val children: List<String>
) : Parcelable