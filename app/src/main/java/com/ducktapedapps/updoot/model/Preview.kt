package com.ducktapedapps.updoot.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Preview(
        val images: List<Images>,
        val enabled: Boolean
) : Parcelable