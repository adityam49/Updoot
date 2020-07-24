package com.ducktapedapps.updoot.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ducktapedapps.updoot.ui.comments.CommentScreenContent
import com.squareup.moshi.Json

@Entity
data class LinkData(
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
        val post_hint: String?,
        @PrimaryKey val id: String,
        val url: String,
        val permalink: String,
        val over_18: Boolean,
        @Embedded val gildings: Gildings,
        @Embedded val imageSet: ImageSet?,
        val lastUpdated: Long,
        val stickied: Boolean,
        val videoUrl: String?
) : CommentScreenContent