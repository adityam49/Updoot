package com.ducktapedapps.updoot.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Gildings(
        @field:SerializedName("gid_1") val silver: Int,
        @field:SerializedName("gid_2") val gold: Int,
        @field:SerializedName("gid_3") val platinum: Int
) : Parcelable