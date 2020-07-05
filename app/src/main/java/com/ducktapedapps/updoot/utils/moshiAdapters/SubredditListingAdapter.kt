package com.ducktapedapps.updoot.utils.moshiAdapters

import com.ducktapedapps.updoot.model.Subreddit
import com.ducktapedapps.updoot.model.SubredditListing
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson

class SubredditListingAdapter {
    private val moshi: Moshi = Moshi.Builder().build()
    private val subredditAdapter: JsonAdapter<Subreddit> = moshi.adapter<Subreddit>(Subreddit::class.java)
    private val mapAdapter = moshi.adapter(Map::class.java)

    @ToJson
    fun serialize(subredditListing: SubredditListing) = ""

    @Suppress("Unchecked_cast")
    @FromJson
    fun deserialize(map: Map<*, *>): SubredditListing {
        val data = map["data"] as Map<String, *>
        val children = data["children"] as List<Map<*, *>>
        val childList = mutableListOf<Subreddit>()
        val after = data["after"] as String?
        children.forEach {
            subredditAdapter.fromJson(mapAdapter.toJson(it["data"] as Map<*, *>))?.let { subreddit -> childList.add(subreddit) }
        }
        return SubredditListing(childList, after)
    }
}
