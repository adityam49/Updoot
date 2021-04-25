package com.ducktapedapps.updoot.navDrawer

import com.ducktapedapps.updoot.data.local.model.LocalMultiReddit
import com.ducktapedapps.updoot.data.remote.model.mapToLocalSubreddit
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

interface GetUserMultiRedditsUseCase {
    val multiReddits: StateFlow<List<LocalMultiReddit>>

    suspend fun loadMultiReddits()
}

class GetUserMultiRedditsUseCaseImpl @Inject constructor(
    private val redditClient: RedditClient,
) : GetUserMultiRedditsUseCase {
    override val multiReddits: MutableStateFlow<List<LocalMultiReddit>> =
        MutableStateFlow(emptyList())

    override suspend fun loadMultiReddits() {
        try {
            val api = redditClient.api()
            val allMultiReddits = api.getUserMultiReddits()
            multiReddits.value = allMultiReddits.map { multiReddit ->
                LocalMultiReddit(
                    multiRedditName = multiReddit.data.displayName,
                    multiRedditIcon = multiReddit.data.icon,
                    subreddits = multiReddit.data.subreddits.map { subs -> subs.data.mapToLocalSubreddit() }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

