package com.ducktapedapps.updoot.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Trophy(
    @Json(name = "icon_70") val icon: String,
    val name: String,
)
