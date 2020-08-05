package com.ducktapedapps.updoot.backgroundWork

import androidx.work.DelegatingWorkerFactory
import com.ducktapedapps.updoot.api.local.SubmissionsCacheDAO
import com.ducktapedapps.updoot.api.local.SubredditDAO
import com.ducktapedapps.updoot.backgroundWork.cacheCleanUp.CacheCleanUpWorkerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdootWorkerFactory @Inject constructor(
        submissionsCacheDAO: SubmissionsCacheDAO,
        subredditDAO: SubredditDAO
) : DelegatingWorkerFactory() {
    init {
        addFactory(CacheCleanUpWorkerFactory(submissionsCacheDAO, subredditDAO))
    }
}