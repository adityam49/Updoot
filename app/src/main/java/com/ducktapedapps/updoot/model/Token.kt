package com.ducktapedapps.updoot.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Token(val access_token: String,
            val refresh_token: String?,
            var absoluteExpiry: Long,
            val token_type: String) : Parcelable {


    fun setAbsoluteExpiry() {
        absoluteExpiry = System.currentTimeMillis() + (3600 * 1000)
    }
}
