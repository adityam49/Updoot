package com.ducktapedapps.updoot.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class BaseComment(
        open val id: String,
        open val depth: Int,
        open val name: String,
        open val parent_id: String
)