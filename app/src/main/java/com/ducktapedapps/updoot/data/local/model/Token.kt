package com.ducktapedapps.updoot.data.local.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
class Token(val access_token: String,
            val refresh_token: String?,
            var absoluteExpiry: Long = System.currentTimeMillis(),
            val token_type: String) : Parcelable {


    fun setAbsoluteExpiry() {
        absoluteExpiry = System.currentTimeMillis() + (3600 * 1000)
    }
}
