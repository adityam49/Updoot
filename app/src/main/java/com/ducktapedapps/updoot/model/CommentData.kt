package com.ducktapedapps.updoot.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CommentData(
        val _id: String,
        val _depth: Int,
        val _parent_id: String,
        val _name: String,
        val author: String,
        var body: String,
        var ups: Int,
        val likes: Boolean?,
        val replies: List<BaseComment>,
        val gildings: Gildings,
        val repliesExpanded: Boolean = false,
        val is_submitter: Boolean
) : Parcelable, BaseComment(_id, _depth, _name, _parent_id) {

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