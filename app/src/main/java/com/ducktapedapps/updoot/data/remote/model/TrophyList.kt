package com.ducktapedapps.updoot.data.remote.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TrophyList(
    val trophies: List<Trophy>
)
