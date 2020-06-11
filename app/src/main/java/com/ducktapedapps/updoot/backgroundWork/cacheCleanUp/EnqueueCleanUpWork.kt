package com.ducktapedapps.updoot.backgroundWork.cacheCleanUp

import android.content.Context
import android.os.Build
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

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) constraints.setRequiresDeviceIdle(true)

    val cleanUpWorkRequest = PeriodicWorkRequestBuilder<SubmissionsCacheCleanUpWorker>(3, TimeUnit.HOURS)
            .addTag(CACHED_SUBMISSIONS_CLEANUP_WORKER_TAG)
            .setConstraints(constraints.build())
            .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_CLEANUP_WORK_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            cleanUpWorkRequest
    )
}