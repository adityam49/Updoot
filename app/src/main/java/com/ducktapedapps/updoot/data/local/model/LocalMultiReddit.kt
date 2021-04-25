package com.ducktapedapps.updoot.data.local.model

data class LocalMultiReddit(
    val multiRedditName: String,
    val multiRedditIcon: String,
    val subreddits: List<LocalSubreddit>,
)
