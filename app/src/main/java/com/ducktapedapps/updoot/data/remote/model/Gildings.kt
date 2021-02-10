package com.ducktapedapps.updoot.data.remote.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Gildings(
        val gid_1: Int = 0,
        val gid_2: Int = 0,
        val gid_3: Int = 0,
)