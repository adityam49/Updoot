package com.ducktapedapps.updoot.backgroundWork.cacheCleanUp

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.ducktapedapps.updoot.api.local.SubmissionsCacheDAO

class SubmissionsCacheCleanUpWorkerFactory(private val submissionsCacheDAO: SubmissionsCacheDAO) : WorkerFactory() {
    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? =
            SubmissionsCacheCleanUpWorker(appContext, workerParameters, submissionsCacheDAO)
}