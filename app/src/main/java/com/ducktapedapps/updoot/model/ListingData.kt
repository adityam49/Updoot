package com.ducktapedapps.updoot.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ListingData(val after: String,
                       val children: List<Thing>) : Data, Parcelable