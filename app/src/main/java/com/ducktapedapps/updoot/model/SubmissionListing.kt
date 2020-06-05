package com.ducktapedapps.updoot.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubmissionListing(
        val after: String?,
        val submissions: List<LinkData>
)
