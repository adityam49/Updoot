package com.ducktapedapps.updoot.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchListing(
        val after: String?,
        val children: List<Subreddit>
)