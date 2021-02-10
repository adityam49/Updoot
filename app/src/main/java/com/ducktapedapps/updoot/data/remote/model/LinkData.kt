package com.ducktapedapps.updoot.data.remote.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LinkData(
        val approved_at_utc: Long?,
        val selftext: String?,
        val title: String,
        val archived: Boolean,
        val author: String,
        val locked: Boolean,
        val ups: Int?,
        val likes: Boolean?,
        val subreddit: String,
        val name: String,
        val thumbnail: String,
        val saved: Boolean,
        val created_utc: Long,
        val num_comments: Int,
        val url: String,
        val permalink: String,
        val over_18: Boolean,
        val gildings: Gildings,
        val preview: ImageVariants?,
        val lastUpdated: Long = 0,
        val stickied: Boolean,
        val media: Video?,
) : RedditThing