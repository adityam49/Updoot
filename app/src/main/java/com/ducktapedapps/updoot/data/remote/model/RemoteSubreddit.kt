package com.ducktapedapps.updoot.data.remote.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity
@JsonClass(generateAdapter = true)
data class RemoteSubreddit(
        @PrimaryKey val display_name: String,
        val community_icon: String = "",
        val subscribers: Long? = 0,
        val accounts_active: Long? = 0,
        val public_description: String?,
        val description: String?,
        val created: Long,
        var lastUpdated: Long?,
)