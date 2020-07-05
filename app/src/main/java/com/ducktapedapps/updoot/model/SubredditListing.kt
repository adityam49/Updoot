package com.ducktapedapps.updoot.model

data class SubredditListing(
        val subreddits: List<Subreddit>,
        val after: String?
)