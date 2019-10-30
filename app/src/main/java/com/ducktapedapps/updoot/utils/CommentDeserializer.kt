package com.ducktapedapps.updoot.utils

import android.util.Log
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
            val author: String = json.get("author")?.asString ?: "null"
            val depth: Int = json.get("depth").asInt
            val body: String = json.get("body")?.asString ?: ""
            val ups: Int = json.get("ups")?.asInt ?: 0

            //TODO fix nullable boolean assignment
            //val likes: Boolean? = json.get("likes")?.asBoolean
            val id: String = json.get("id").asString
            val gildings: Gildings = context?.deserialize(json.get("gildings"), Gildings::class.java)
                    ?: Gildings(0, 0, 0)

            val replyList = mutableListOf<CommentData>()
            val replyListing = json.get("replies")
            if (replyListing is JsonObject) {
                val repliesData = replyListing.get("data")?.asJsonObject?.get("children")?.asJsonArray
                if (repliesData != null) {
                    for (childReply in repliesData) {
                        if (childReply?.asJsonObject?.get("data") != null) {
                            Log.i(TAG, "reply for ${childReply.asJsonObject.get("data")}")
                            val data: CommentData? = context?.deserialize(childReply.asJsonObject.get("data"), CommentData::class.java)
                            if (data != null) replyList.add(data)
                        }
                    }
                }
            }
            return CommentData(
                    author,
                    depth,
                    body,
                    ups,
                    //TODO voted comments won't be marked
                    null,
                    id,
                    replyList.toList(),
                    gildings,
                    false
            )
        }
        return null
    }
}