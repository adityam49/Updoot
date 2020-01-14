package com.ducktapedapps.updoot.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Images(
        val source: Source,
        val resolutions: List<Resolution>
) : Parcelable


