package com.ducktapedapps.updoot.utils

import android.util.Log
import com.ducktapedapps.updoot.model.SearchListing
import com.ducktapedapps.updoot.model.Subreddit
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi

class SearchAdapter {
    private val moshi = Moshi.Builder().build()
    private val mapAdapter = moshi.adapter(Map::class.java)
    private val subredditAdapter = moshi.adapter(Subreddit::class.java)

    @FromJson
    fun formJson(map: Map<*, *>): SearchListing? {
        return if (map["kind"] == "Listing") {
            val data = map["data"] as Map<*, *>
            val after = map["after"] as String?
            val children = data["children"] as? List<Map<*, *>> ?: listOf()
            Log.i(this.javaClass.simpleName, "children size is ${children.size}")
            return SearchListing(after,
                    children
                            .filter { it["kind"] == "t5" }
                            .map { it["data"] as Map<*, *> }
                            .mapNotNull { subredditAdapter.fromJson(mapAdapter.toJson(it)) })
        } else null
    }
}