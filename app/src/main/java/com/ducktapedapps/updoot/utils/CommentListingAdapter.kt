package com.ducktapedapps.updoot.utils


import com.ducktapedapps.updoot.model.CommentData
import com.ducktapedapps.updoot.model.CommentListing
import com.ducktapedapps.updoot.model.Gildings
import com.ducktapedapps.updoot.model.MoreCommentData
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi

class CommentListingAdapter {
    private val moshi = Moshi.Builder().build()
    private val mapAdapter = moshi.adapter(Map::class.java)
    private val gildingsAdapter = moshi.adapter(Gildings::class.java)
    private val moreCommentsAdapter = moshi.adapter(MoreCommentData::class.java)

    @FromJson
    fun fromJson(postAndSubmission: List<Map<*, *>>): CommentListing? {
        return if (postAndSubmission.size == 2) {
            fromJsonCommentDetail(postAndSubmission[1])
        } else null
    }


    private fun fromJsonCommentDetail(jsonMap: Map<*, *>): CommentListing? {

        val data = jsonMap["data"] as? Map<*, *> ?: return null

        val children = data["children"] as? List<Map<*, *>> ?: return null

        // remove post from children
        val commentList = children.filter {
            it["kind"] == "t1"
        }.map {
            deserializeCommentDetail(it)
        }

        val moreCommentData = children.filter {
            it["kind"] == "more"
        }.map {
            val moreData = it["data"] as Map<*, *>
            moreCommentsAdapter.fromJson(mapAdapter.toJson(moreData))
        }.firstOrNull()

        return CommentListing(commentList, moreCommentData)
    }

    private fun deserializeCommentDetail(json: Map<*, *>): CommentData {
        val data = json["data"] as Map<*, *>

        // replies can be json obj or empty string
        val replies = data["replies"] as? Map<*, *>

        return CommentData(
                author = data["author"] as? String ?: "Unknown",
                body = data["body"] as? String ?: "",
                gildings = gildingsAdapter.fromJson(mapAdapter.toJson(data["gildings"] as Map<*, *>))
                        ?: Gildings(),
                id = data["id"] as? String ?: "",
                ups = (data["ups"] as? Double)?.toInt() ?: 0,
                replies = if (replies != null) {
                    fromJsonCommentDetail(replies)
                } else {
                    null
                },
                depth = (data["depth"] as? Double)?.toInt() ?: 0,
                repliesExpanded = false,
                is_submitter = data["is_submitter"] as? Boolean ?: false,
                likes = data["likes"] as Boolean?,
                parent_id = data["parent_id"] as String,
                name = data["name"] as String
        )
    }
}