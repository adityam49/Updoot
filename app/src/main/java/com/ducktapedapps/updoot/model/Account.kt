package com.ducktapedapps.updoot.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Account(@field:SerializedName("name") val name: String) : Parcelable
