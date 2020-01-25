package com.ducktapedapps.updoot.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
open class BaseComment(
        val id: String,
        val depth: Int,
        val name: String,
        val parent_id: String
) : Parcelable