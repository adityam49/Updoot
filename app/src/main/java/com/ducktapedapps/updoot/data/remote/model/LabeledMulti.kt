package com.ducktapedapps.updoot.data.remote.model

import com.ducktapedapps.updoot.data.local.model.LocalSubreddit
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class LabeledMulti(
    val data: LabeledMultiData
)

@JsonClass(generateAdapter = true)
data class LabeledMultiData(
    @Json(name = "display_name") val displayName: String,
    @Json(name = "icon_url") val icon: String,
    val subreddits: List<MultiSubreddit>,
)

@JsonClass(generateAdapter = true)
data class MultiSubreddit(
    val data: MultiSubredditData,
)

@JsonClass(generateAdapter = true)
data class MultiSubredditData(
    val community_icon: String? = "",
    val public_description: String,
    val description: String,
    val display_name: String,
    val created: Long,
    val subscribers: Long,
)

fun MultiSubredditData.mapToLocalSubreddit(): LocalSubreddit {
    return LocalSubreddit(
        subredditName = display_name,
        icon = community_icon ?: "",
        subscribers = subscribers,
        accountsActive = 0,
        longDescription = description,
        shortDescription = public_description,
        created = Date(created),
        lastUpdated = Date()
    )
}