package com.ducktapedapps.updoot.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LinkData(
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
) : Data, Parcelable {

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

    fun toggleSelfTextExpansion(): LinkData {
        return this.copy(isSelfTextExpanded = !this.isSelfTextExpanded)
    }
}