package com.ducktapedapps.updoot.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class CommentData(
        val id: String,
        val depth: Int,
        val parent_id: String,
        val author: String,
        val name: String,
        var body: String,
        var ups: Int,
        val likes: Boolean?,
        val replies: CommentListing?,
        val gildings: Gildings,
        val repliesExpanded: Boolean = false,
        val is_submitter: Boolean
) : Parcelable {

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