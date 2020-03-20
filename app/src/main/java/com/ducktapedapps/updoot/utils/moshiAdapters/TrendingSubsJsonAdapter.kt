package com.ducktapedapps.updoot.utils.moshiAdapters

import com.squareup.moshi.FromJson

class TrendingSubsJsonAdapter {
    @FromJson
    fun fromJson(map: Map<*, *>): List<String> = map["subreddit_names"] as? List<String> ?: listOf()
}