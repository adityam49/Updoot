package com.ducktapedapps.updoot.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class LinkData(
        @field:SerializedName("selftext_html") val selftext: String?,
        val title: String,
        val archived: Boolean,
        val author: String,
        val locked: Boolean,
        val ups: Int,
        val likes: Boolean?,
        @field:SerializedName("subreddit_name_prefixed") val subredditName: String,
        val name: String,
        val thumbnail: String,
        val saved: Boolean,
        @field:SerializedName("created_utc") val created: Long,
        @field:SerializedName("num_comments") val commentsCount: Int,
        val gildings: Gildings,
        val preview: Preview?,
        val post_hint: String?,
        val id: String,
        val url: String,
        val isSelfTextExpanded: Boolean = false
) : Data, Serializable {
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
        return LinkData(
                title = this.title,
                archived = this.archived,
                author = this.author,
                locked = this.locked,
                ups = updatedUps,
                likes = updatedLikes,
                subredditName = this.subredditName,
                name = this.name,
                thumbnail = this.thumbnail,
                saved = this.saved,
                created = this.created,
                commentsCount = this.commentsCount,
                gildings = this.gildings,
                preview = this.preview,
                post_hint = this.post_hint,
                id = this.id,
                url = this.url,
                isSelfTextExpanded = this.isSelfTextExpanded,
                selftext = this.selftext
        )
    }

    fun save(): LinkData {
        return LinkData(
                title = this.title,
                archived = this.archived,
                author = this.author,
                locked = this.locked,
                ups = this.ups,
                likes = this.likes,
                subredditName = this.subredditName,
                name = this.name,
                thumbnail = this.thumbnail,
                saved = !this.saved,
                created = this.created,
                commentsCount = this.commentsCount,
                gildings = this.gildings,
                preview = this.preview,
                post_hint = this.post_hint,
                id = this.id,
                url = this.url,
                isSelfTextExpanded = this.isSelfTextExpanded,
                selftext = this.selftext
        )
    }

    fun toggleSelfTextExpansion(): LinkData {
        return LinkData(
                title = this.title,
                archived = this.archived,
                author = this.author,
                locked = this.locked,
                ups = this.ups,
                likes = this.likes,
                subredditName = this.subredditName,
                name = this.name,
                thumbnail = this.thumbnail,
                saved = !this.saved,
                created = this.created,
                commentsCount = this.commentsCount,
                gildings = this.gildings,
                preview = this.preview,
                post_hint = this.post_hint,
                id = this.id,
                url = this.url,
                isSelfTextExpanded = !this.isSelfTextExpanded,
                selftext = this.selftext
        )
    }
}