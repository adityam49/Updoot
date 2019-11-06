package com.ducktapedapps.updoot.utils

import com.ducktapedapps.updoot.model.CommentData
import com.ducktapedapps.updoot.model.Gildings
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

const val TAG = "CommentDeserializer"

class CommentDeserializer : JsonDeserializer<CommentData> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): CommentData? {


        if (json != null && json is JsonObject) {
            // parrent_id , id , depth are common to commentData and loadMoreComments type
            val parent_id = json.get("parent_id").asString
            val id: String = json.get("id").asString
            val depth: Int = json.get("depth").asInt

            //only for commentData
            val author: String = json.get("author")?.asString ?: ""
            var body: String = json.get("body")?.asString ?: ""
            val ups: Int = json.get("ups")?.asInt ?: 0
            //TODO fix nullable boolean assignment
            //val likes: Boolean? = json.get("likes")?.asBoolean
            val gildings: Gildings = context?.deserialize(json.get("gildings"), Gildings::class.java)
                    ?: Gildings(0, 0, 0)

            val replyList = mutableListOf<CommentData>()
            val replyListing = json.get("replies")
            if (replyListing is JsonObject) {
                val repliesData = replyListing.get("data")?.asJsonObject?.get("children")?.asJsonArray
                if (repliesData != null) {
                    for (childReply in repliesData) {
                        if (childReply?.asJsonObject?.get("data") != null) {
                            val data: CommentData? = context?.deserialize(childReply.asJsonObject.get("data"), CommentData::class.java)
                            if (data != null) replyList.add(data)
                        }
                    }
                }
            }
            val is_submitter: Boolean = json.get("is_submitter")?.asBoolean ?: false
            var count: Int? = null
            var children: List<String> = listOf()
            if (body.isEmpty() && author.isEmpty()) {
                //only for loadMoreComments
                count = json.get("count")?.asInt
                if (count != 0) body = "Load $count more replies"
                children = listOf()
            }

            return CommentData(
                    author = author,
                    depth = depth,
                    body = body,
                    ups = ups,
                    //TODO voted comments won't be marked
                    likes = null,
                    id = id,
                    is_submitter = is_submitter,
                    replies = replyList.toList(),
                    gildings = gildings,
                    repliesExpanded = false,
                    parent_id = parent_id,
                    count = count,
                    loadMoreChildren = children
            )
        }
        return null
    }
}