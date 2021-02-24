package com.ducktapedapps.updoot.data.remote.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class Listing<T>(
        val after: String? = null,
        val children: List<T>
)