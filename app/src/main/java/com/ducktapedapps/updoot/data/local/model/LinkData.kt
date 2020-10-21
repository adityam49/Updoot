package com.ducktapedapps.updoot.data.local.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Entity
@JsonClass(generateAdapter = true)
data class LinkData(
        val approved_at_utc: Long?,
        val selftext: String?,
        val title: String,
        val archived: Boolean,
        val author: String,
        val locked: Boolean,
        val ups: Int,
        val likes: Boolean?,
        @Json(name = "subreddit") val subredditName: String,
        @PrimaryKey val name: String,
        val thumbnail: String,
        val saved: Boolean,
        @Json(name = "created_utc") val created: Long,
        @Json(name = "num_comments") val commentsCount: Int,
        val url: String,
        val permalink: String,
        val over_18: Boolean,
        @Embedded val gildings: Gildings,
        @Embedded val preview: ImageVariants?,
        val lastUpdated: Long = 0,
        val stickied: Boolean,
        @Embedded @Json(name = "media") val video: Video?
) : RedditThing