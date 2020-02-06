package com.ducktapedapps.updoot.utils

import com.ducktapedapps.updoot.model.Subreddit
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi

class SubredditInfoAdapter {
    private val moshi: Moshi = Moshi.Builder().build()
    private val mapAdapter = moshi.adapter(Map::class.java)
    private val subredditAdapter = moshi.adapter(Subreddit::class.java)
    @FromJson
    fun fromJson(map: Map<*, *>): Subreddit? = if (map["kind"] == "t5") {
        val dataMap = map["data"] as Map<*, *>
        subredditAdapter.fromJson(mapAdapter.toJson(dataMap))
    } else null

}