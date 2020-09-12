package com.ducktapedapps.updoot.backgroundWork

import androidx.work.DelegatingWorkerFactory
import com.ducktapedapps.updoot.data.local.SubmissionsCacheDAO
import com.ducktapedapps.updoot.data.local.SubredditDAO
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdootWorkerFactory @Inject constructor(
        submissionsCacheDAO: SubmissionsCacheDAO,
        subredditDAO: SubredditDAO,
        redditClient: RedditClient
) : DelegatingWorkerFactory() {
    init {
        addFactory(SubscriptionSyncWorkerFactory(subredditDAO,redditClient))
        addFactory(CacheCleanUpWorkerFactory(submissionsCacheDAO,subredditDAO))
    }
}