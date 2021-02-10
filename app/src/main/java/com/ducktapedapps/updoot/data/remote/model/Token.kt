package com.ducktapedapps.updoot.data.remote.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class Token(
        val access_token: String,
        val refresh_token: String?,
        var absoluteExpiry: Long = System.currentTimeMillis(),
        val token_type: String
) {
    fun setAbsoluteExpiry() {
        absoluteExpiry = System.currentTimeMillis() + (3600 * 1000)
    }
}
