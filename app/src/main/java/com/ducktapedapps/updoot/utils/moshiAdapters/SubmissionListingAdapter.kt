package com.ducktapedapps.updoot.utils.moshiAdapters

import com.ducktapedapps.updoot.model.SubmissionListing
import com.squareup.moshi.FromJson

class SubmissionListingAdapter {
    private val linkDataAdapter = LinkDataAdapter()

    @Suppress("UNCHECKED_CAST")
    @FromJson
    fun fromJson(jsonMap: Map<*, *>): SubmissionListing? {

        if (jsonMap["kind"] != "Listing") return null

        val data = jsonMap["data"] as? Map<*, *> ?: return null

        val children = data["children"] as? List<Map<*, *>> ?: return null

        if (children.any { it["kind"] != "t3" }) return null

        val submissions = children.mapNotNull {
            if (it["data"] != null) linkDataAdapter.deserializeSubmissionDetail(it["data"] as Map<*, *>) else null
        }
        return SubmissionListing(data["after"] as String?, submissions)
    }
}
