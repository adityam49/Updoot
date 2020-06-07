package com.ducktapedapps.updoot.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Preview(
        val images: List<Images>,
        val enabled: Boolean
)