package com.ducktapedapps.updoot.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CommentData(
        //common for normal comments and load more comments
        val id: String,
        val depth: Int,
        val parent_id: String,

        val author: String,
        val name: String,
        var body: String,
        var ups: Int,
        val likes: Boolean?,
        val replies: List<CommentData>,
        val gildings: Gildings,
        val repliesExpanded: Boolean = false,
        val is_submitter: Boolean,

        //only for load more comments
        val count: Int?,
        @SerializedName("children")
        val loadMoreChildren: List<String>?
) : Data, Parcelable {

    fun vote(direction: Int): CommentData {
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
            -1 -> when {
                this.likes == null -> {
                    updatedUps--
                    updatedLikes = false
                }
                this.likes -> {
                    updatedUps -= 2
                    updatedLikes = false
                }
                else -> {
                    updatedUps++
                    updatedLikes = null
                }
            }
        }
        return this.copy(
                ups = updatedUps,
                likes = updatedLikes
        )
    }
}