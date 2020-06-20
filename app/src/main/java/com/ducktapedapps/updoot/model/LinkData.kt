package com.ducktapedapps.updoot.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ducktapedapps.updoot.ui.comments.CommentScreenContent
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Entity
@JsonClass(generateAdapter = true)
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
        val lastUpdated: Long? = null
) : CommentScreenContent {

    fun vote(direction: Int): LinkData {
        var updatedLikes: Boolean? = this.likes
        var updatedUps = this.ups
        when (direction) {
            1 -> if (this.likes == null) {
                updatedLikes = true
                updatedUps++
            } else if (!this.likes) {
                updatedLikes = true
                updatedUps += 2
            } else {
                updatedLikes = null
                updatedUps--
            }
            -1 -> if (this.likes == null) {
                updatedUps--
                updatedLikes = false
            } else if (this.likes) {
                updatedUps -= 2
                updatedLikes = false
            } else {
                updatedUps++
                updatedLikes = null
            }
        }
        return this.copy(
                ups = updatedUps,
                likes = updatedLikes
        )
    }

    fun save(): LinkData {
        return this.copy(saved = !this.saved)
    }
}