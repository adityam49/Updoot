package com.ducktapedapps.updoot.utils

import com.ducktapedapps.updoot.model.BaseComment
import com.ducktapedapps.updoot.model.CommentData
import com.ducktapedapps.updoot.model.Gildings
import com.ducktapedapps.updoot.model.MoreComments
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi

class MoreCommentsListAdapter {

    private val moshi = Moshi.Builder().build()
    private val mapAdapter = moshi.adapter(Map::class.java)
    private val gildingsAdapter = moshi.adapter(Gildings::class.java)

    @FromJson
    fun fromJson(map: Map<*, *>): MoreComments {
        val json = map["json"] as? Map<*, *>
                ?: throw JsonDataException("json : invalid moreChildren api response")
        val data = json["data"] as? Map<*, *>
                ?: throw JsonDataException("data : invalid moreChildren api response")
        val things = data["things"] as? List<Map<*, *>>
                ?: throw JsonDataException("things : invalid moreChildren api response")
        val flattenedComments = things
                .filter { it["kind"] == "t1" }
                .map { deserializeCommentDetail(it) }
        return MoreComments(indent(flattenedComments))
    }

    private fun indent(flattenedComments: List<CommentData>): List<BaseComment> {
        //TODO : properly unflatten comments

        //just returning top level comments loaded from load more comments
        val maxDepth = flattenedComments.first().depth
        return flattenedComments.filter { it.depth == maxDepth }
    }


    private fun deserializeCommentDetail(json: Map<*, *>): CommentData {
        val data = json["data"] as Map<*, *>

        // replies can be json obj or empty string

        return CommentData(
                author = data["author"] as? String ?: "Unknown",
                body = data["body"] as? String ?: "",
                gildings = gildingsAdapter.fromJson(mapAdapter.toJson(data["gildings"] as Map<*, *>))
                        ?: Gildings(),
                _id = data["id"] as String,
                ups = (data["ups"] as? Double)?.toInt() ?: 0,
                replies = listOf(),
                _depth = (data["depth"] as? Double)?.toInt() ?: 0,
                repliesExpanded = false,
                is_submitter = data["is_submitter"] as? Boolean ?: false,
                likes = data["likes"] as Boolean?,
                _parent_id = data["parent_id"] as String,
                _name = data["name"] as String
        )
    }
}