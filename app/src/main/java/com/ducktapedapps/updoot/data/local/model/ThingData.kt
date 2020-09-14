package com.ducktapedapps.updoot.data.local.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ducktapedapps.updoot.data.local.moshiAdapters.Thing
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

sealed class ThingData

@JsonClass(generateAdapter = true)
data class ListingThing(
        val after: String?,
        val children: List<Thing>
) : ThingData()

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
        val name: String,
        val thumbnail: String,
        val saved: Boolean,
        @Json(name = "created_utc") val created: Long,
        @Json(name = "num_comments") val commentsCount: Int,
        @PrimaryKey val id: String,
        val url: String,
        val permalink: String,
        val over_18: Boolean,
        @Embedded val gildings: Gildings,
        @Embedded val preview: ImageVariants?,
        val lastUpdated: Long = 0,
        val stickied: Boolean,
        @Embedded @Json(name = "media") val video: Video?
) : ThingData()

@Entity
@JsonClass(generateAdapter = true)
data class Subreddit(
        @PrimaryKey val display_name: String,
        val community_icon: String = "",
        val subscribers: Long? = 0,
        val active_user_count: Long? = 0,
        val public_description: String?,
        val description: String?,
        val created: Long,
        var lastUpdated: Long?,
        val isTrending: Int = 0 //0 is not trending 1 is trending
) : ThingData()

sealed class BaseComment : ThingData()

@JsonClass(generateAdapter = true)
data class CommentData(
        val id: String,
        val depth: Int,
        val parent_id: String,
        val name: String,
        val author: String,
        var body: String?,
        var ups: Int?,
        val likes: Boolean?,
        val replies: Thing?,
        val gildings: Gildings,
        val repliesExpanded: Boolean = false,
        val is_submitter: Boolean,
        val author_flair_text: String? = ""
) : BaseComment()

@JsonClass(generateAdapter = true)
data class MoreCommentData(
        val count: Int,
        val name: String,
        val id: String,
        val parent_id: String,
        val depth: Int,
        val replies: Thing?
) : BaseComment()