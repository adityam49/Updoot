package com.ducktapedapps.updoot.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Subreddit(
        val display_name_prefixed: String,
        val icon_img: String,
        val subscribers: Long,
        val public_description: String
) : Parcelable, Data