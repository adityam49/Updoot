package com.ducktapedapps.updoot.utils

import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.model.SubmissionListing
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi


class SubmissionListingAdapter {
    private val moshi = Moshi.Builder().build()
    private val linkDataAdapter: JsonAdapter<LinkData> = moshi.adapter(LinkData::class.java)
    private val mapAdapter = moshi.adapter(Map::class.java)

    @FromJson
    fun fromJson(jsonMap: Map<*, *>): SubmissionListing? {

        if (jsonMap["kind"] != "Listing") return null

        val data = jsonMap["data"] as? Map<*, *> ?: return null

        val children = data["children"] as? List<Map<*, *>> ?: return null

        if (children.any { it["kind"] != "t3" }) return null

        val submissions = children.mapNotNull {
            if (it["data"] != null) linkDataAdapter.fromJson(mapAdapter.toJson(it["data"] as Map<*, *>)) else null
        }
        return SubmissionListing(data["after"] as String?, submissions)
    }
}
