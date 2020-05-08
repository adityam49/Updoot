package com.ducktapedapps.updoot.model

import android.text.Spanned

data class CommentData(
        val _id: String,
        val _depth: Int,
        val _parent_id: String,
        val _name: String,
        val author: String,
        var body: Spanned,
        var ups: Int?,
        val likes: Boolean?,
        val replies: List<BaseComment>,
        val gildings: Gildings,
        val repliesExpanded: Boolean = false,
        val is_submitter: Boolean,
        val author_flair_text: String
) : BaseComment(_id, _depth, _name, _parent_id) {

    fun vote(direction: Int): CommentData {
        var updatedLikes: Boolean? = this.likes
        var updatedUps = this.ups
        when (direction) {
            1 -> if (this.likes == null) {
                updatedLikes = true
                if (updatedUps != null)
                    updatedUps++
            } else if (!this.likes) {
                updatedLikes = true
                if (updatedUps != null)
                    updatedUps += 2
            } else {
                updatedLikes = null
                if (updatedUps != null)
                    updatedUps--
            }
            -1 -> when {
                this.likes == null -> {
                    if (updatedUps != null)
                        updatedUps--
                    updatedLikes = false
                }
                this.likes -> {
                    if (updatedUps != null)
                        updatedUps -= 2
                    updatedLikes = false
                }
                else -> {
                    if (updatedUps != null)
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