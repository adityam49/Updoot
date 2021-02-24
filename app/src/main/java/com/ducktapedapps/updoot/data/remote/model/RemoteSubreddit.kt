package com.ducktapedapps.updoot.data.remote.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemoteSubreddit(
        val display_name: String,
        val community_icon: String = "",
        val subscribers: Long? = 0,
        val accounts_active: Long? = 0,
        val public_description: String?,
        val description: String?,
        val created: Long,
)