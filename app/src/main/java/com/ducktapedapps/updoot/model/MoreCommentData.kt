package com.ducktapedapps.updoot.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MoreCommentData(
        val count: Int,
        @Json(name = "name") override val name: String,
        @Json(name = "id") override val id: String,
        @Json(name = "parent_id") override val parent_id: String,
        @Json(name = "depth") override val depth: Int,
        val children: List<String>
) : BaseComment(id, depth, name, parent_id)