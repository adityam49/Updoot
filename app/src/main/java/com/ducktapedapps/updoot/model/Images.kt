package com.ducktapedapps.updoot.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Images(val source: Source,
                  val resolutions: List<Resolution>) : Parcelable

