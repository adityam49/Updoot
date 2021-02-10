package com.ducktapedapps.updoot.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class LocalSubreddit(
        @PrimaryKey val subredditName: String,
        val icon: String = "",
        val subscribers: Long? = 0,
        val accountsActive: Long? = 0,
        val shortDescription: String?,
        val longDescription: String?,
        val created: Date,
        var lastUpdated: Date,
)