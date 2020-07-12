package com.ducktapedapps.updoot.backgroundWork

import androidx.work.DelegatingWorkerFactory
import com.ducktapedapps.updoot.api.local.SubmissionsCacheDAO
import com.ducktapedapps.updoot.backgroundWork.cacheCleanUp.SubmissionsCacheCleanUpWorkerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdootWorkerFactory @Inject constructor(
        submissionsCacheDAO: SubmissionsCacheDAO
) : DelegatingWorkerFactory() {
    init {
        addFactory(SubmissionsCacheCleanUpWorkerFactory(submissionsCacheDAO))
    }
}