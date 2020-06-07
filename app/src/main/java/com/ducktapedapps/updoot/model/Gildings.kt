package com.ducktapedapps.updoot.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Gildings(
        @Json(name = "gid_1") val silver: Int = 0,
        @Json(name = "gid_2") val gold: Int = 0,
        @Json(name = "gid_3") val platinum: Int = 0
)