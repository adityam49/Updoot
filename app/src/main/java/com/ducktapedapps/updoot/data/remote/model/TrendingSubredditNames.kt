package com.ducktapedapps.updoot.data.remote.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TrendingSubredditNames(val subreddit_names: List<String>)