package com.ducktapedapps.updoot.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Thing(
        val kind: String,
        val data: Data
) : Parcelable