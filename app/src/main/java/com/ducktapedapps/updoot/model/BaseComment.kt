package com.ducktapedapps.updoot.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class BaseComment(
        val id: String,
        val depth: Int,
        val name: String,
        val parent_id: String
)