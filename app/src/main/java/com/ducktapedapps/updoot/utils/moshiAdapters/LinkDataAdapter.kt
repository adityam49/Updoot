package com.ducktapedapps.updoot.utils.moshiAdapters

import com.ducktapedapps.updoot.model.Gildings
import com.ducktapedapps.updoot.model.LinkData
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class LinkDataAdapter {
    @ToJson
    fun serializeSubmissionDetail(linkData: LinkData): String = ""

    @Suppress("UNCHECKED_CAST")
    @FromJson
    fun deserializeSubmissionDetail(data: Map<*, *>): LinkData {
        return LinkData(
                selftext = data["selftext"] as String? ?: "",
                title = data["title"] as String,
                archived = data["archived"] as Boolean,
                author = data["author"] as String,
                locked = data["locked"] as Boolean,
                ups = (data["ups"] as Double).toInt(),
                likes = data["likes"] as Boolean?,
                subredditName = data["subreddit"] as String,
                name = data["name"] as String,
                thumbnail = data["thumbnail"] as? String ?: "",
                saved = data["saved"] as Boolean,
                created = (data["created_utc"] as Double).toLong(),
                commentsCount = (data["num_comments"] as Double).toInt(),
                id = data["id"] as String,
                url = data["url"] as String,
                permalink = data["permalink"] as String,
                over_18 = data["over_18"] as Boolean,
                gildings = getGildings(data["gildings"] as Map<String, *>),
                lastUpdated = System.currentTimeMillis() / 1000,
                post_hint = data["post_hint"] as String?
        )
    }

    private fun getGildings(map: Map<String, *>): Gildings =
            Gildings(
                    silver = (map["gid_1"] as? Double)?.toInt() ?: 0,
                    gold = (map["gid_2"] as? Double)?.toInt() ?: 0,
                    platinum = (map["gid_3"] as? Double)?.toInt() ?: 0
            )
}