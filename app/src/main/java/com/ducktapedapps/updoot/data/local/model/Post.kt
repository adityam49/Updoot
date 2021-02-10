package com.ducktapedapps.updoot.data.local.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Post(
        @PrimaryKey val id: String,
        val title: String,
        val author: String,
        val upVotes: Int?,
        val userHasUpVoted: Boolean? = null,
        val saved: Boolean,
        val archived: Boolean,
        val locked: Boolean,
        val stickied: Boolean,
        val isNsfw: Boolean,
        val subredditName: String,
        val created: Date,
        val commentsCount: Int,
        val postContentUrl: String,
        val permalink: String,
        @Embedded val gildings: Gildings,
        val postThumbnail: String?,

        //media
        @Embedded val mediaImage: ImageVariants?,
        @Embedded val mediaVideo: Video?,
        val mediaText: String? = null,

        val lastUpdated: Date,
)