package com.ducktapedapps.updoot.backgroundWork.cacheCleanUp

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

private const val UNIQUE_CLEANUP_WORK_TAG = "unique_cleanup_work_tag"
private const val CACHED_SUBMISSIONS_CLEANUP_WORKER_TAG = "cached_submissions_cleanup_worker_tag"
fun enqueueCleanUpWork(context: Context) {
    val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

    val cleanUpWorkRequest = PeriodicWorkRequestBuilder<SubmissionsCacheCleanUpWorker>(3, TimeUnit.HOURS)
            .addTag(CACHED_SUBMISSIONS_CLEANUP_WORKER_TAG)
            .setConstraints(constraints)
            .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_CLEANUP_WORK_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            cleanUpWorkRequest
    )
}