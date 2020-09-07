package com.ducktapedapps.updoot.backgroundWork

import android.content.Context
import android.os.Build
import androidx.work.*
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.api.local.SubmissionsCacheDAO
import com.ducktapedapps.updoot.api.local.SubredditDAO
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.model.Subreddit
import com.ducktapedapps.updoot.utils.Constants.FRONTPAGE
import com.ducktapedapps.updoot.utils.createNotification
import java.util.concurrent.TimeUnit

class CacheCleanUpWorker(
        private val context: Context,
        workParas: WorkerParameters,
        private val submissionsCacheDAO: SubmissionsCacheDAO,
        private val subredditDAO: SubredditDAO
) : CoroutineWorker(context, workParas) {
    override suspend fun doWork(): Result =
            try {
                val currentTimeInSeconds = System.currentTimeMillis() / 1000
                findAndRemoveStaleSubmissions(currentTimeInSeconds)
                findAndRemoveStaleSubreddits(currentTimeInSeconds)
                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                showFailureReportNotification(e)
                Result.failure()
            }

    @Throws(Exception::class)
    private suspend fun findAndRemoveStaleSubmissions(currentTimeInSeconds: Long) {
        var totalSubmissions: Int
        var submissionsRemoved = 0
        submissionsCacheDAO.getAllCachedSubmissions().apply {
            totalSubmissions = size
            forEach {
                if (it.isStale(currentTimeInSeconds)) {
                    submissionsCacheDAO.deleteSubmission(it.id, it.subredditName)
                    submissionsRemoved++
                }
            }
            showSubmissionsRemovedNotification(totalSubmissions, submissionsRemoved)
        }
    }

    private fun showSubmissionsRemovedNotification(total: Int, removed: Int) =
            with(context) {
                createNotification(
                        resources.getString(R.string.Submission_cache_cleanup),
                        resources.getString(R.string.total_remove_submissions, total, removed, total - removed),
                        this
                )
            }

    private fun LinkData.isStale(currentTimeInSeconds: Long): Boolean =
            SUBMISSION_STALE_THRESHOLD_IN_HOURS * 60 * 60 < currentTimeInSeconds - lastUpdated

    @Throws(Exception::class)
    private suspend fun findAndRemoveStaleSubreddits(currentTimeInSeconds: Long) {
        var subredditsRemovedCount = 0
        subredditDAO.getNonSubscribedSubreddits().filter {
            it.isStale(currentTimeInSeconds) && it.display_name != FRONTPAGE
        }.forEach {
            subredditsRemovedCount++
            subredditDAO.deleteSubreddit(it.display_name)
        }
        showSubredditsRemovedNotification(subredditsRemovedCount)
    }

    private fun showSubredditsRemovedNotification(count: Int) {
        with(context) {
            createNotification(
                    getString(R.string.subreddit_cache_removed),
                    getString(R.string.subreddit_removed_count, count),
                    context
            )
        }
    }

    private fun Subreddit?.isStale(currentTimeInSeconds: Long): Boolean {
        return this == null ||
                SUBREDDIT_STALE_THRESHOLD_IN_HOURS * 60 * 60 <
                currentTimeInSeconds - (lastUpdated ?: 0L)
    }

    private fun showFailureReportNotification(e: Exception) = with(context) {
        createNotification(
                resources.getString(R.string.something_went_wrong),
                e.message.toString(),
                this
        )
    }

    companion object {
        const val CACHED_SUBMISSIONS_CLEANUP_WORKER_TAG = "cached_submissions_cleanup_worker_tag"
        private const val SUBREDDIT_STALE_THRESHOLD_IN_HOURS = 48
        private const val SUBMISSION_STALE_THRESHOLD_IN_HOURS = 6
    }
}

class CacheCleanUpWorkerFactory(
        private val submissionsCacheDAO: SubmissionsCacheDAO,
        private val subredditDAO: SubredditDAO
) : WorkerFactory() {
    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? =
            if (workerClassName == CacheCleanUpWorker::class.java.name)
                CacheCleanUpWorker(appContext, workerParameters, submissionsCacheDAO, subredditDAO)
            else null
}

fun Context.enqueueCleanUpWork() {
    val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) constraints.setRequiresDeviceIdle(true)

    val workRequest = PeriodicWorkRequestBuilder<CacheCleanUpWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints.build())
            .build()
    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            CacheCleanUpWorker.CACHED_SUBMISSIONS_CLEANUP_WORKER_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
    )
}